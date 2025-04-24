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

import com.example.vjutest.Model.Major;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.SubjectRepository;

@Service
public class MajorService {

    private static final Logger logger = LoggerFactory.getLogger(MajorService.class);
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final EntityManager entityManager;

    @Autowired
    public MajorService(MajorRepository majorRepository,
                       DepartmentRepository departmentRepository,
                       UserRepository userRepository,
                       SubjectRepository subjectRepository,
                       EntityManager entityManager) {
        this.majorRepository = majorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.entityManager = entityManager;
    }

    public Major createMajor(String name, Long departmentId, Long userId) {
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ quản trị viên mới có thể tạo ngành");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa: " + departmentId));

        Major major = new Major();
        major.setName(name);
        major.setDepartment(department);
        major.setCreatedBy(createBy);
        major.setModifiedBy(createBy);

        return majorRepository.save(major);
    }

    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    public List<Major> getMajorsByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa: " + departmentId));
        return majorRepository.findByDepartment(department);
    }

    public Optional<Major> getMajorById(Long id) {
        return majorRepository.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Major updateMajor(Long majorId, String name, Long departmentId, Long userId) {
        User updateBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    
        if (!updateBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ quản trị viên mới có thể cập nhật ngành!");
        }
    
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành!"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa!"));
    
        major.setName(name);
        major.setDepartment(department);
        major.setModifiedBy(updateBy);
    
        return majorRepository.save(major);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "majors", allEntries = true)
    public void deleteMajor(Long majorId, Long userId) {
        logger.info("Bắt đầu xóa ngành với ID: {} bởi người dùng ID: {}", majorId, userId);
        
        try {
            User deleteBy = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy người dùng với ID: {}", userId);
                        return new RuntimeException("Không tìm thấy người dùng: " + userId);
                    });
        
            if (!deleteBy.getRole().getName().equalsIgnoreCase("admin")) {
                logger.error("Người dùng {} không có quyền xóa ngành. Role: {}", userId, deleteBy.getRole().getName());
                throw new RuntimeException("Chỉ quản trị viên mới có thể xóa ngành");
            }
        
            Major major = majorRepository.findById(majorId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy ngành với ID: {}", majorId);
                        return new RuntimeException("Không tìm thấy ngành với ID: " + majorId);
                    });

            // Kiểm tra xem ngành có môn học nào không
            int subjectCount = subjectRepository.countByMajorsContaining(major);
            if (subjectCount > 0) {
                String errorMessage = String.format(
                    "Không thể xóa ngành vì đang có %d môn học thuộc ngành này.",
                    subjectCount
                );
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
                
            // Xóa trực tiếp bằng native query để đảm bảo
            entityManager.createNativeQuery("DELETE FROM majors WHERE id = :id")
                .setParameter("id", majorId)
                .executeUpdate();
            
            // Flush và clear cache
            entityManager.flush();
            entityManager.clear();
            
            logger.info("Đã xóa thành công ngành {}", majorId);
            
        } catch (Exception e) {
            logger.error("Lỗi khi xóa ngành {}: {}", majorId, e.getMessage(), e);
            throw e;
        }
    }
} 