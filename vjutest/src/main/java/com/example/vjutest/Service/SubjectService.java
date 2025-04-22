package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import jakarta.persistence.EntityManager;

import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.ClassSubjectRepository;
import com.example.vjutest.Repository.ExamRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.UserRepository;

@Service
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final EntityManager entityManager;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository, 
                          ExamRepository examRepository, QuestionRepository questionRepository,
                          ClassSubjectRepository classSubjectRepository, EntityManager entityManager) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.entityManager = entityManager;
    }

    public Subject createSubject(String name, String subjectCode, String description, Integer creditHour , Long userId) {
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("teacher") && !createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể tạo môn học");
        }

        Subject newSubject = new Subject(name, subjectCode, description, creditHour, createBy);

        return subjectRepository.save(newSubject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Subject updateSubject(Long subjectId, String name, String subjectCode, String description, Integer creditHour, Long userId) {
        User updateBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    
        if (!updateBy.getRole().getName().equalsIgnoreCase("teacher") && !updateBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể cập nhật môn học!");
        }
    
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
    
        // Kiểm tra mã môn học có trùng với môn học khác không
        Subject subjectWithSameCode = subjectRepository.findBySubjectCode(subjectCode).orElse(null);
        if (subjectWithSameCode != null && !subjectWithSameCode.getId().equals(subjectId)) {
            throw new RuntimeException("Mã môn học đã tồn tại cho môn học khác");
        }
    
        subject.setName(name);
        subject.setSubjectCode(subjectCode);
        subject.setDescription(description);
        subject.setCreditHour(creditHour);
    
        return subjectRepository.save(subject);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "subjects", allEntries = true)
    public void deleteSubject(Long subjectId, Long userId) {
        logger.info("Bắt đầu xóa môn học với ID: {} bởi người dùng ID: {}", subjectId, userId);
        
        try {
            User deleteBy = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy người dùng với ID: {}", userId);
                        return new RuntimeException("Không tìm thấy người dùng: " + userId);
                    });
        
            if (!deleteBy.getRole().getName().equalsIgnoreCase("admin") && !deleteBy.getRole().getName().equalsIgnoreCase("teacher")) {
                logger.error("Người dùng {} không có quyền xóa môn học. Role: {}", userId, deleteBy.getRole().getName());
                throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể xóa môn học");
            }
        
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy môn học với ID: {}", subjectId);
                        return new RuntimeException("Không tìm thấy môn học với ID: " + subjectId);
                    });

            logger.info("Kiểm tra các ràng buộc của môn học {} ({}):", subjectId, subject.getSubjectCode());
            int examCount = examRepository.countBySubject(subject);
            int questionCount = questionRepository.countBySubject(subject);
            int classSubjectCount = classSubjectRepository.countBySubject(subject);
        
            logger.info("Số lượng ràng buộc - Đề thi: {}, Câu hỏi: {}, Lớp học: {}", 
                examCount, questionCount, classSubjectCount);
        
            if (examCount > 0 || questionCount > 0 || classSubjectCount > 0) {
                String errorMessage = String.format(
                    "Không thể xóa môn học vì đang được sử dụng trong %d đề thi, %d câu hỏi, và %d lớp học.",
                    examCount, questionCount, classSubjectCount
                );
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
                
            // Xóa trực tiếp bằng native query để đảm bảo
            entityManager.createNativeQuery("DELETE FROM subjects WHERE id = :id")
                .setParameter("id", subjectId)
                .executeUpdate();
            
            // Flush và clear cache
            entityManager.flush();
            entityManager.clear();
            
            logger.info("Đã xóa thành công môn học {} ({})", subjectId, subject.getSubjectCode());
            
        } catch (Exception e) {
            logger.error("Lỗi khi xóa môn học {}: {}", subjectId, e.getMessage(), e);
            throw e;
        }
    }
    
}
