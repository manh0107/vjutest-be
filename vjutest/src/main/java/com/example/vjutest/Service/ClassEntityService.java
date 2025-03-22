package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Repository.UserRepository;

@Service
public class ClassEntityService {

    @Autowired
    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;

    public ClassEntityService(ClassEntityRepository classEntityRepository, UserRepository userRepository) {
        this.classEntityRepository = classEntityRepository;
        this.userRepository = userRepository;
    }

    // Kiểm tra user có quyền thao tác trên lớp không (chỉ giáo viên tạo lớp hoặc admin)
    private boolean isAuthorized(Long userId, ClassEntity classEntity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));

        String role = user.getRole().getName().toLowerCase();
        return role.equals("admin") || classEntity.getCreatedBy().getId().equals(userId);
    }

    // Tạo lớp học
    @Transactional
    public ClassEntity createClass(String name, String classCode, String description, Long userId) {
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));

        if (!createdBy.getRole().getName().equalsIgnoreCase("teacher") && !createdBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể tạo lớp học");
        }

        ClassEntity newClassEntity = new ClassEntity(name, classCode, description, createdBy);
        newClassEntity.getTeachers().add(createdBy);
        return classEntityRepository.save(newClassEntity);
    }

    // Lấy danh sách lớp học
    public List<ClassEntity> getAllClasses() {
        return classEntityRepository.findAll();
    }

    // Lấy lớp học theo ID
    public Optional<ClassEntity> getClassById(Long id) {
        return classEntityRepository.findById(id);
    }

    // Cập nhật thông tin lớp học
    @Transactional
    public ClassEntity updateClass(Long id, ClassEntity classEntity, Long userId) {
        ClassEntity existingClass = classEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));
        if (!isAuthorized(userId, existingClass)) {
            throw new RuntimeException("Bạn không có quyền cập nhật lớp học này");
        }
        existingClass.setName(classEntity.getName());
        existingClass.setDescription(classEntity.getDescription());

        if (!existingClass.getClassCode().equals(classEntity.getClassCode())) {
            boolean isDuplicate = classEntityRepository.existsByClassCode(classEntity.getClassCode());
            if (isDuplicate) {
                throw new RuntimeException("Mã lớp đã tồn tại, vui lòng chọn mã khác");
            }
            existingClass.setClassCode(classEntity.getClassCode());
        }
        return classEntityRepository.save(existingClass);
    }

    // Xóa lớp học (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void deleteClass(Long id, Long userId) {
        ClassEntity existingClass = classEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, existingClass)) {
            throw new RuntimeException("Bạn không có quyền xóa lớp học này");
        }

        classEntityRepository.delete(existingClass);
    }

    // Thêm học sinh vào lớp (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void addStudentToClass(Long classId, Long studentId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new RuntimeException("Bạn không có quyền thêm học sinh vào lớp này");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        classEntity.getUsers().add(student);
        classEntityRepository.save(classEntity);
    }

    // Xóa học sinh khỏi lớp (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new RuntimeException("Bạn không có quyền xóa học sinh khỏi lớp này");
        }

        classEntity.getUsers().removeIf(student -> student.getId().equals(studentId));
        classEntityRepository.save(classEntity);
    }

    //Mời giáo viên vào lớp
    @Transactional
    public void inviteTeacher(Long classId, Long inviterId, Long inviteeId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("Người mời không tồn tại"));
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new RuntimeException("Giáo viên được mời không tồn tại"));
        
        if (!classEntity.isTeacherOfClass(inviter)) {
            throw new RuntimeException("Bạn không có quyền mời giáo viên vào lớp này");
        }
        
        if (!invitee.getRole().getName().equalsIgnoreCase("teacher")) {
            throw new RuntimeException("Chỉ có thể mời giáo viên vào lớp");
        }
        
        classEntity.getTeachers().add(invitee);
        classEntityRepository.save(classEntity);
    }
}
