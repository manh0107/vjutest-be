package com.example.vjutest.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

import com.example.vjutest.Model.Department;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Exception.ResourceNotFoundException;
import com.example.vjutest.Exception.UnauthorizedException;
import com.example.vjutest.Exception.DuplicateDepartmentNameException;


@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final EntityManager entityManager;
 
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public Department createDepartment(String name, Long userId) {
        logger.debug("Tạo khoa mới với tên: {} bởi người dùng ID: {}", name, userId);
        
        // Validate name
        validateDepartmentName(name);
        
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedException("Chỉ quản trị viên mới có thể tạo khoa");
        }

        // Check if department name already exists
        if (departmentRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ValidationException("Tên khoa đã tồn tại");
        }

        Department department = new Department();
        department.setName(name);
        department.setCreatedBy(createBy);
        department.setModifiedBy(createBy);

        Department savedDepartment = departmentRepository.save(department);
        logger.info("Đã tạo khoa mới với ID: {}", savedDepartment.getId());
        return savedDepartment;
    }

    @Cacheable(value = "departments")
    public List<Department> getAllDepartments() {
        logger.debug("Lấy danh sách tất cả các khoa");
        return departmentRepository.findAll();
    }

    public Page<Department> getAllDepartmentsPaged(Pageable pageable) {
        logger.debug("Lấy danh sách khoa theo trang: {}", pageable);
        return departmentRepository.findAll(pageable);
    }

    @Cacheable(value = "departments", key = "#id")
    public Optional<Department> getDepartmentById(Long id) {
        logger.debug("Lấy thông tin khoa với ID: {}", id);
        return departmentRepository.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public Department updateDepartment(Long departmentId, String name, Long userId) {
        logger.debug("Cập nhật khoa ID: {} với tên: {} bởi người dùng ID: {}", departmentId, name, userId);
        
        // Validate name
        validateDepartmentName(name);
        
        User updateBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
    
        if (!updateBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedException("Chỉ quản trị viên mới có thể cập nhật khoa!");
        }
    
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa!"));
        
        // Check if new name already exists for other departments
        Optional<Department> departmentWithSameName = departmentRepository.findByNameIgnoreCase(name);
        if (departmentWithSameName.isPresent() && !Objects.equals(departmentWithSameName.get().getId(), Long.valueOf(departmentId))) {
            throw new DuplicateDepartmentNameException("Department with name '" + name + "' already exists");
        }
    
        department.setName(name);
        department.setModifiedBy(updateBy);
    
        Department updatedDepartment = departmentRepository.save(department);
        logger.info("Đã cập nhật khoa ID: {}", departmentId);
        return updatedDepartment;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Long departmentId, Long userId) {
        logger.info("Bắt đầu xóa khoa với ID: {} bởi người dùng ID: {}", departmentId, userId);
        
        try {
            User deleteBy = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy người dùng với ID: {}", userId);
                        return new ResourceNotFoundException("Không tìm thấy người dùng: " + userId);
                    });
        
            if (!deleteBy.getRole().getName().equalsIgnoreCase("admin")) {
                logger.error("Người dùng {} không có quyền xóa khoa. Role: {}", userId, deleteBy.getRole().getName());
                throw new UnauthorizedException("Chỉ quản trị viên mới có thể xóa khoa");
            }
        
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> {
                        logger.error("Không tìm thấy khoa với ID: {}", departmentId);
                        return new ResourceNotFoundException("Không tìm thấy khoa với ID: " + departmentId);
                    });

            // Kiểm tra xem khoa có ngành nào không
            int majorCount = majorRepository.countByDepartment(department);
            if (majorCount > 0) {
                String errorMessage = String.format(
                    "Không thể xóa khoa vì đang có %d ngành thuộc khoa này.",
                    majorCount
                );
                logger.error(errorMessage);
                throw new ValidationException(errorMessage);
            }
                
            // Xóa trực tiếp bằng native query để đảm bảo
            entityManager.createNativeQuery("DELETE FROM departments WHERE id = :id")
                .setParameter("id", departmentId)
                .executeUpdate();
            
            // Flush và clear cache
            entityManager.flush();
            entityManager.clear();
            
            logger.info("Đã xóa thành công khoa {}", departmentId);
            
        } catch (Exception e) {
            logger.error("Lỗi khi xóa khoa {}: {}", departmentId, e.getMessage(), e);
            throw e;
        }
    }

    private void validateDepartmentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tên khoa không được để trống");
        }
        if (name.length() < 2) {
            throw new ValidationException("Tên khoa phải có ít nhất 2 ký tự");
        }
        if (name.length() > 100) {
            throw new ValidationException("Tên khoa không được vượt quá 100 ký tự");
        }
    }
} 