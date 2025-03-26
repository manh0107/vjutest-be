package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.JoinRequestRepository;

@Service
public class ClassEntityService {

    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserMapper userMapper;

    @Autowired
    public ClassEntityService(ClassEntityRepository classEntityRepository, UserRepository userRepository, JoinRequestRepository joinRequestRepository, UserMapper userMapper) {
        this.classEntityRepository = classEntityRepository;
        this.userRepository = userRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userMapper = userMapper;
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

    // Thêm sinh viên vào lớp (chỉ giáo viên tạo lớp hoặc admin)
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

    // Xóa sinh viên khỏi lớp (chỉ giáo viên tạo lớp hoặc admin)
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

    //Sinh viên yêu cầu tham gia lớp học
    @Transactional
    public String requestToJoin(Long studentId, Long classId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        ClassEntity classEntity = classEntityRepository.findById(classId).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));

        if (!user.getRole().getName().equals("student")) {
            throw new RuntimeException("Chỉ sinh viên mới có thể tham gia lớp học!");
        }

        Optional<JoinRequest> existingRequest = joinRequestRepository.findByUserAndClassEntity(user, classEntity);
        if (existingRequest.isPresent()) {
            throw new RuntimeException("Bạn đã gửi yêu cầu tham gia lớp học này rồi!");
        }

        JoinRequest joinRequest = new JoinRequest(user, classEntity, JoinRequest.Status.PENDING, JoinRequest.Type.STUDENT_REQUEST);
        joinRequestRepository.save(joinRequest);
        return "Yêu cầu tham gia lớp học đã được gửi!";
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

        if (inviterId.equals(inviteeId)) {
            throw new RuntimeException("Bạn không thể tự mời chính mình vào lớp!");
        }        

        // Kiểm tra xem đã có lời mời chưa
        Optional<JoinRequest> existingRequest = joinRequestRepository.findByUserAndClassEntity(invitee, classEntity);
        if (existingRequest.isPresent()) {
            throw new RuntimeException("Đã gửi lời mời cho giáo viên này!");
        }

        // Lưu lời mời vào bảng JoinRequest
        JoinRequest inviteRequest = new JoinRequest(invitee, classEntity, JoinRequest.Status.PENDING, JoinRequest.Type.TEACHER_INVITE);
        joinRequestRepository.save(inviteRequest);
    }

    //Sinh viên rời lớp học
    @Transactional
    public String leaveClass(Long classId, Long studentId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại"));

        if (!student.getRole().getName().equalsIgnoreCase("student")) {
            throw new RuntimeException("Chỉ sinh viên mới có thể rời lớp học");
        }

        if (!classEntity.getUsers().contains(student)) {
            throw new RuntimeException("Bạn không phải là thành viên của lớp này");
        }

        classEntity.getUsers().remove(student);
        classEntityRepository.save(classEntity);
        return "Bạn đã rời khỏi lớp học!";
    }

    // Lấy danh sách sinh viên trong lớp học
    public List<UserDTO> getStudentsInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        return classEntity.getUsers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList());
    }

    // Lấy danh sách giáo viên trong lớp học
    public List<UserDTO> getTeachersInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        return classEntity.getTeachers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList());
    }
}
