package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.vjutest.Model.Chapter;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.ChapterRepository;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Exception.UnauthorizedAccessException;
import com.example.vjutest.Exception.ResourceNotFoundException;

@Service
public class ChapterService {

    private static final Logger logger = LoggerFactory.getLogger(ChapterService.class);
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public ChapterService(ChapterRepository chapterRepository, SubjectRepository subjectRepository,
                         UserRepository userRepository, QuestionRepository questionRepository) {
        this.chapterRepository = chapterRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    private boolean hasAccessToSubject(User user, Subject subject) {
        // Admin có quyền truy cập tất cả
        if ("admin".equals(user.getRole().getName())) {
            return true;
        }

        // Kiểm tra visibility của subject
        switch (subject.getVisibility()) {
            case PUBLIC:
                // Public: tất cả đều có thể xem
                return true;
            case DEPARTMENT:
                // Department: chỉ những người cùng khoa mới xem được
                return subject.getMajors().stream()
                        .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            case MAJOR:
                // Major: chỉ những người cùng ngành mới xem được
                return subject.getMajors().stream()
                        .anyMatch(major -> major.equals(user.getMajor()));
            default:
                return false;
        }
    }

    @Transactional
    public Chapter createChapter(String name, Long subjectId, Long userId) {
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("teacher") && !createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedAccessException("Chỉ giáo viên và quản trị viên mới có thể tạo chương học");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học: " + subjectId));

        // Kiểm tra quyền truy cập
        if (!hasAccessToSubject(createBy, subject)) {
            throw new UnauthorizedAccessException("Bạn không có quyền tạo chương học cho môn học này!");
        }

        Chapter newChapter = new Chapter();
        newChapter.setName(name);
        newChapter.setSubject(subject);
        newChapter.setCreatedBy(createBy);
        newChapter.setModifiedBy(createBy);

        return chapterRepository.save(newChapter);
    }

    public List<Chapter> getAllChapters(Long subjectId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học: " + subjectId));

        // Kiểm tra quyền truy cập
        if (!hasAccessToSubject(user, subject)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem chương học của môn học này!");
        }

        return chapterRepository.findBySubject(subject);
    }

    public Optional<Chapter> getChapterById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Optional<Chapter> chapterOpt = chapterRepository.findById(id);
        if (chapterOpt.isEmpty()) {
            return Optional.empty();
        }

        Chapter chapter = chapterOpt.get();
        Subject subject = chapter.getSubject();

        // Kiểm tra quyền truy cập
        if (!hasAccessToSubject(user, subject)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem chương học này!");
        }

        return Optional.of(chapter);
    }

    @Transactional
    public Chapter updateChapter(Long chapterId, String name, Long userId) {
        User updateBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
    
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học!"));
    
        Subject subject = chapter.getSubject();

        // Kiểm tra quyền cập nhật
        if (!"admin".equals(updateBy.getRole().getName()) && 
            !(updateBy.getRole().getName().equals("teacher") && chapter.getCreatedBy().equals(updateBy))) {
            throw new UnauthorizedAccessException("Bạn không có quyền cập nhật chương học này!");
        }

        // Kiểm tra quyền truy cập môn học
        if (!hasAccessToSubject(updateBy, subject)) {
            throw new UnauthorizedAccessException("Bạn không có quyền cập nhật chương học này!");
        }

        chapter.setName(name);
        chapter.setModifiedBy(updateBy);

        return chapterRepository.save(chapter);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long chapterId, Long userId) {
        logger.info("Bắt đầu xóa chương học với ID: {} bởi người dùng ID: {}", chapterId, userId);
        
        try {
            User deleteBy = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
            if (!deleteBy.getRole().getName().equalsIgnoreCase("admin") && 
                !deleteBy.getRole().getName().equalsIgnoreCase("teacher")) {
                throw new UnauthorizedAccessException("Chỉ giáo viên và quản trị viên mới có thể xóa chương học");
            }
        
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + chapterId));

            Subject subject = chapter.getSubject();

            // Kiểm tra quyền truy cập
            if (!hasAccessToSubject(deleteBy, subject)) {
                throw new UnauthorizedAccessException("Bạn không có quyền xóa chương học này!");
            }

            // Kiểm tra xem chương học có câu hỏi nào không
            int questionCount = questionRepository.countByChapter(chapter);
            if (questionCount > 0) {
                throw new UnauthorizedAccessException("Không thể xóa chương học vì chương học đang có câu hỏi!");
            }

            chapterRepository.delete(chapter);
            logger.info("Xóa chương học thành công");
        } catch (Exception e) {
            logger.error("Lỗi khi xóa chương học: {}", e.getMessage());
            throw e;
        }
    }
} 