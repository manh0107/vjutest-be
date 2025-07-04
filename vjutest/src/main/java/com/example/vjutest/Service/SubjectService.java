package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import jakarta.persistence.EntityManager;

import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Repository.ClassSubjectRepository;
import com.example.vjutest.Repository.ExamRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Exception.UnauthorizedAccessException;
import com.example.vjutest.Exception.ResourceNotFoundException;

@Service
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final EntityManager entityManager;
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository, 
                          ExamRepository examRepository, QuestionRepository questionRepository,
                          ClassSubjectRepository classSubjectRepository, EntityManager entityManager,
                          MajorRepository majorRepository, DepartmentRepository departmentRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.entityManager = entityManager;
        this.majorRepository = majorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Subject createSubject(String name, String subjectCode, String description, Integer creditHour, 
                               Long userId, List<Long> majorIds, List<Long> departmentIds, Subject.VisibilityScope visibility) {
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("teacher") && !createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedAccessException("Chỉ giáo viên và quản trị viên mới có thể tạo môn học");
        }

        Subject newSubject = new Subject();
        newSubject.setName(name);
        newSubject.setSubjectCode(subjectCode);
        newSubject.setDescription(description);
        newSubject.setCreditHour(creditHour);
        newSubject.setCreatedBy(createBy);
        newSubject.setModifiedBy(createBy);
        newSubject.setVisibility(visibility);

        if (visibility == Subject.VisibilityScope.DEPARTMENT) {
            if (departmentIds == null || departmentIds.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất một khoa cho môn học theo khoa.");
            }
            Set<com.example.vjutest.Model.Department> depts = new java.util.HashSet<>(departmentRepository.findAllById(departmentIds));
            newSubject.setDepartments(depts);
        }
        if (visibility == Subject.VisibilityScope.MAJOR) {
            if (majorIds == null || majorIds.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất một ngành cho môn học theo ngành.");
            }
            Set<Major> majors = new java.util.HashSet<>(majorRepository.findAllById(majorIds));
            newSubject.setMajors(majors);
        }
        // Nếu PUBLIC thì không cần set gì thêm

        return subjectRepository.save(newSubject);
    }

    public List<Subject> getAllSubjects(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        if ("admin".equals(user.getRole().getName())) {
            return subjectRepository.findAll();
        }

        // Lọc môn học theo department và major của người dùng
        return subjectRepository.findAll().stream()
                .filter(subject -> {
                    if (subject.getVisibility() == Subject.VisibilityScope.PUBLIC) {
                        return true;
                    }
                    if (subject.getVisibility() == Subject.VisibilityScope.DEPARTMENT) {
                        return subject.getDepartments() != null && 
                               user.getDepartment() != null &&
                               subject.getDepartments().contains(user.getDepartment());
                    }
                    return subject.getMajors().stream()
                            .anyMatch(major -> major.equals(user.getMajor()));
                })
                .collect(Collectors.toList());
    }

    public Optional<Subject> getSubjectById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Optional<Subject> subjectOpt = subjectRepository.findById(id);
        if (subjectOpt.isEmpty()) {
            return Optional.empty();
        }

        Subject subject = subjectOpt.get();
        if ("admin".equals(user.getRole().getName())) {
            return Optional.of(subject);
        }

        // Kiểm tra quyền truy cập
        if (subject.getVisibility() == Subject.VisibilityScope.PUBLIC) {
            return Optional.of(subject);
        }
        if (subject.getVisibility() == Subject.VisibilityScope.DEPARTMENT) {
            if (subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()))) {
                return Optional.of(subject);
            }
        } else {
            if (subject.getMajors().stream()
                    .anyMatch(major -> major.equals(user.getMajor()))) {
                return Optional.of(subject);
            }
        }

        return Optional.empty();
    }

    @Transactional
    public Subject updateSubject(Long subjectId, String name, String subjectCode, String description, 
                               Integer creditHour, Long userId, List<Long> majorIds, List<Long> departmentIds, Subject.VisibilityScope visibility) {
        User updateBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
    
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học!"));
    
        // Kiểm tra quyền cập nhật
        if (!"admin".equals(updateBy.getRole().getName()) && 
            !(updateBy.getRole().getName().equals("teacher") && subject.getCreatedBy().equals(updateBy))) {
            throw new UnauthorizedAccessException("Bạn không có quyền cập nhật môn học này!");
        }

        subject.setName(name);
        subject.setSubjectCode(subjectCode);
        subject.setDescription(description);
        subject.setCreditHour(creditHour);
        subject.setModifiedBy(updateBy);
        subject.setVisibility(visibility);

        // Xóa các liên kết cũ
        subject.getDepartments().clear();
        subject.getMajors().clear();

        if (visibility == Subject.VisibilityScope.DEPARTMENT) {
            if (departmentIds == null || departmentIds.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất một khoa cho môn học theo khoa.");
            }
            Set<com.example.vjutest.Model.Department> depts = new java.util.HashSet<>(departmentRepository.findAllById(departmentIds));
            subject.setDepartments(depts);
        }
        if (visibility == Subject.VisibilityScope.MAJOR) {
            if (majorIds == null || majorIds.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất một ngành cho môn học theo ngành.");
            }
            Set<Major> majors = new java.util.HashSet<>(majorRepository.findAllById(majorIds));
            subject.setMajors(majors);
        }
        // Nếu PUBLIC thì không cần set gì thêm

        return subjectRepository.save(subject);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "subjects", allEntries = true)
    public void deleteSubject(Long subjectId, Long userId) {
        logger.info("Bắt đầu xóa môn học với ID: {} bởi người dùng ID: {}", subjectId, userId);
        
        try {
            User deleteBy = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
            if (!deleteBy.getRole().getName().equalsIgnoreCase("admin") && 
                !deleteBy.getRole().getName().equalsIgnoreCase("teacher")) {
                throw new UnauthorizedAccessException("Chỉ giáo viên và quản trị viên mới có thể xóa môn học");
            }
        
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId));

            // Kiểm tra quyền truy cập
            if (!"admin".equals(deleteBy.getRole().getName())) {
                if (subject.getVisibility() == Subject.VisibilityScope.DEPARTMENT) {
                    if (!subject.getMajors().stream()
                            .anyMatch(major -> major.getDepartment().equals(deleteBy.getDepartment()))) {
                        throw new UnauthorizedAccessException("Bạn không có quyền xóa môn học này!");
                    }
                } else {
                    if (!subject.getMajors().stream()
                            .anyMatch(major -> major.equals(deleteBy.getMajor()))) {
                        throw new UnauthorizedAccessException("Bạn không có quyền xóa môn học này!");
                    }
                }
            }

            logger.info("Kiểm tra các ràng buộc của môn học {} ({}):", subjectId, subject.getSubjectCode());
            int examCount = examRepository.countBySubject(subject);
            int questionCount = questionRepository.countByChapter_Subject(subject);
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

    public long countAllSubjects() {
        return subjectRepository.count();
    }
}
