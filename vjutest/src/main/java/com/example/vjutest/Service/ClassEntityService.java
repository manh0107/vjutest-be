package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.ClassSubject;

import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.Repository.JoinRequestRepository;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.ClassSubjectRepository;
import com.example.vjutest.Exception.UnauthorizedAccessException;
import com.example.vjutest.Exception.ResourceNotFoundException;

@Service
public class ClassEntityService {

    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final UserMapper userMapper;
    private final GoogleDriveService googleDriveService;

    @Autowired
    public ClassEntityService(ClassEntityRepository classEntityRepository, UserRepository userRepository, 
                            JoinRequestRepository joinRequestRepository, DepartmentRepository departmentRepository,
                            MajorRepository majorRepository, SubjectRepository subjectRepository,
                            ClassSubjectRepository classSubjectRepository, UserMapper userMapper,
                            GoogleDriveService googleDriveService) {
        this.classEntityRepository = classEntityRepository;
        this.userRepository = userRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.departmentRepository = departmentRepository;
        this.majorRepository = majorRepository;
        this.subjectRepository = subjectRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.userMapper = userMapper;
        this.googleDriveService = googleDriveService;
    }

    // Kiểm tra user có quyền thao tác trên lớp không (chỉ giáo viên tạo lớp hoặc admin)
    private boolean isAuthorized(Long userId, ClassEntity classEntity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        String role = user.getRole().getName().toLowerCase();
        if (role.equals("admin")) {
            return true;
        }

        // Kiểm tra quyền truy cập dựa trên department và major (nhiều-nhiều)
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            return classEntity.getDepartments().stream().anyMatch(d -> d.equals(user.getDepartment()));
        } else if (classEntity.getVisibility() == ClassEntity.VisibilityScope.MAJOR) {
            return classEntity.getMajors().stream().anyMatch(m -> m.equals(user.getMajor()));
        }

        return classEntity.getCreatedBy().getId().equals(userId);
    }

    // Tạo lớp học
    @Transactional
    public ClassEntity createClass(String name, String classCode, String description, Long userId, List<Long> departmentIds, List<Long> majorIds, ClassEntity.VisibilityScope visibility) {
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        if (!createdBy.getRole().getName().equalsIgnoreCase("teacher") && 
            !createdBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedAccessException("Chỉ giáo viên và quản trị viên mới có thể tạo lớp học");
        }

        if (classEntityRepository.existsByClassCode(classCode)) {
            throw new RuntimeException("Mã lớp đã tồn tại, vui lòng chọn mã khác");
        }

        ClassEntity newClassEntity = new ClassEntity();
        newClassEntity.setName(name);
        newClassEntity.setClassCode(classCode);
        newClassEntity.setDescription(description);
        newClassEntity.setCreatedBy(createdBy);
        newClassEntity.getTeachers().add(createdBy);
        newClassEntity.setVisibility(visibility);

        if (visibility == ClassEntity.VisibilityScope.DEPARTMENT && (departmentIds == null || departmentIds.isEmpty())) {
            throw new RuntimeException("Phải chọn ít nhất một khoa khi phạm vi truy cập là theo khoa");
        }

        if (visibility == ClassEntity.VisibilityScope.MAJOR && (majorIds == null || majorIds.isEmpty())) {
            throw new RuntimeException("Phải chọn ít nhất một ngành khi phạm vi truy cập là theo ngành");
        }

        if (departmentIds != null && !departmentIds.isEmpty()) {
            Set<Department> departments = new java.util.HashSet<>(departmentRepository.findAllById(departmentIds));
            newClassEntity.setDepartments(departments);
        }
        if (majorIds != null && !majorIds.isEmpty()) {
            Set<Major> majors = new java.util.HashSet<>(majorRepository.findAllById(majorIds));
            newClassEntity.setMajors(majors);
        }

        return classEntityRepository.save(newClassEntity);
    }

    // Lấy danh sách lớp học
    public List<ClassEntity> getAllClasses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        // Lọc lớp học theo department và major của người dùng (nhiều-nhiều)
        return classEntityRepository.findAll().stream()
                .filter(classEntity -> {
                    if (classEntity.getVisibility() == ClassEntity.VisibilityScope.PUBLIC) {
                        return true;
                    }
                    if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
                        return classEntity.getDepartments().stream().anyMatch(d -> d.equals(user.getDepartment()));
                    }
                    return classEntity.getMajors().stream().anyMatch(m -> m.equals(user.getMajor()));
                })
                .collect(Collectors.toList());
    }

    // Lấy lớp học theo ID
    public Optional<ClassEntity> getClassById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Optional<ClassEntity> classOpt = classEntityRepository.findById(id);
        if (classOpt.isEmpty()) {
            return Optional.empty();
        }

        ClassEntity classEntity = classOpt.get();
        if ("admin".equals(user.getRole().getName())) {
            return Optional.of(classEntity);
        }

        // Kiểm tra quyền truy cập (nhiều-nhiều)
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.PUBLIC) {
            return Optional.of(classEntity);
        }
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            if (classEntity.getDepartments().stream().anyMatch(d -> d.equals(user.getDepartment()))) {
                return Optional.of(classEntity);
            }
        } else {
            if (classEntity.getMajors().stream().anyMatch(m -> m.equals(user.getMajor()))) {
                return Optional.of(classEntity);
            }
        }

        return Optional.empty();
    }

    // Cập nhật thông tin lớp học
    @Transactional
    public ClassEntity updateClass(
            Long id,
            String name,
            String description,
            String classCode,
            Long userId,
            List<Long> departmentIds,
            List<Long> majorIds,
            ClassEntity.VisibilityScope visibility) {
        ClassEntity existingClass = classEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, existingClass)) {
            throw new UnauthorizedAccessException("Bạn không có quyền cập nhật lớp học này");
        }

        // Validate visibility and selections
        if (visibility == ClassEntity.VisibilityScope.DEPARTMENT && (departmentIds == null || departmentIds.isEmpty())) {
            throw new RuntimeException("Phải chọn ít nhất một khoa khi phạm vi truy cập là theo khoa");
        }

        if (visibility == ClassEntity.VisibilityScope.MAJOR) {
            if (departmentIds == null || departmentIds.isEmpty()) {
                throw new RuntimeException("Phải chọn ít nhất một khoa khi phạm vi truy cập là theo ngành");
            }
            if (majorIds == null || majorIds.isEmpty()) {
                throw new RuntimeException("Phải chọn ít nhất một ngành khi phạm vi truy cập là theo ngành");
            }
        }

        existingClass.setName(name);
        existingClass.setDescription(description);
        existingClass.setVisibility(visibility);

        if (!existingClass.getClassCode().equals(classCode)) {
            boolean isDuplicate = classEntityRepository.existsByClassCode(classCode);
            if (isDuplicate) {
                throw new RuntimeException("Mã lớp đã tồn tại, vui lòng chọn mã khác");
            }
            existingClass.setClassCode(classCode);
        }

        // Update departments and majors
        if (departmentIds != null) {
            Set<Department> departments = new java.util.HashSet<>(departmentRepository.findAllById(departmentIds));
            existingClass.setDepartments(departments);
        }
        if (majorIds != null) {
            Set<Major> majors = new java.util.HashSet<>(majorRepository.findAllById(majorIds));
            existingClass.setMajors(majors);
        }

        return classEntityRepository.save(existingClass);
    }

    // Xóa lớp học (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void deleteClass(Long id, Long userId) {
        ClassEntity existingClass = classEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, existingClass)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xóa lớp học này");
        }

        classEntityRepository.delete(existingClass);
    }

    // Thêm sinh viên vào lớp (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void addStudentToClass(Long classId, Long studentId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền thêm học sinh vào lớp này");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));

        // Kiểm tra sinh viên có thuộc cùng department/major không
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            if (!classEntity.getDepartments().stream().anyMatch(d -> d.equals(student.getDepartment()))) {
                throw new UnauthorizedAccessException("Sinh viên không thuộc cùng khoa với lớp học");
            }
        } else if (classEntity.getVisibility() == ClassEntity.VisibilityScope.MAJOR) {
            if (!classEntity.getMajors().stream().anyMatch(m -> m.equals(student.getMajor()))) {
                throw new UnauthorizedAccessException("Sinh viên không thuộc cùng ngành với lớp học");
            }
        }

        classEntity.getUsers().add(student);
        classEntityRepository.save(classEntity);
    }

    // Xóa sinh viên khỏi lớp (chỉ giáo viên tạo lớp hoặc admin)
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xóa học sinh khỏi lớp này");
        }

        classEntity.getUsers().removeIf(student -> student.getId().equals(studentId));
        classEntityRepository.save(classEntity);
    }

    //Sinh viên yêu cầu tham gia lớp học
    @Transactional
    public String requestToJoin(Long studentId, Long classId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        if (!user.getRole().getName().equals("student")) {
            throw new UnauthorizedAccessException("Chỉ sinh viên mới có thể tham gia lớp học!");
        }

        // Kiểm tra sinh viên có thuộc cùng department/major không
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            if (!classEntity.getDepartments().stream().anyMatch(d -> d.equals(user.getDepartment()))) {
                throw new UnauthorizedAccessException("Bạn không thuộc cùng khoa với lớp học");
            }
        } else if (classEntity.getVisibility() == ClassEntity.VisibilityScope.MAJOR) {
            if (!classEntity.getMajors().stream().anyMatch(m -> m.equals(user.getMajor()))) {
                throw new UnauthorizedAccessException("Bạn không thuộc cùng ngành với lớp học");
            }
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
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));
        
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("Người mời không tồn tại"));
        
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new ResourceNotFoundException("Giáo viên được mời không tồn tại"));

        if (!isAuthorized(inviter.getId(), classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền mời giáo viên vào lớp này");
        }

        if (!invitee.getRole().getName().equalsIgnoreCase("teacher")) {
            throw new UnauthorizedAccessException("Chỉ có thể mời giáo viên vào lớp");
        }

        if (inviterId.equals(inviteeId)) {
            throw new RuntimeException("Bạn không thể tự mời chính mình vào lớp!");
        }

        // Kiểm tra giáo viên có thuộc cùng department/major không
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            if (!classEntity.getDepartments().stream().anyMatch(d -> d.equals(invitee.getDepartment()))) {
                throw new UnauthorizedAccessException("Giáo viên không thuộc cùng khoa với lớp học");
            }
        } else if (classEntity.getVisibility() == ClassEntity.VisibilityScope.MAJOR) {
            if (!classEntity.getMajors().stream().anyMatch(m -> m.equals(invitee.getMajor()))) {
                throw new UnauthorizedAccessException("Giáo viên không thuộc cùng ngành với lớp học");
            }
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
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên không tồn tại"));

        if (!student.getRole().getName().equalsIgnoreCase("student")) {
            throw new UnauthorizedAccessException("Chỉ sinh viên mới có thể rời lớp học");
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
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        return classEntity.getUsers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList());
    }

    // Lấy danh sách giáo viên trong lớp học
    public List<UserDTO> getTeachersInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        return classEntity.getTeachers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList());
    }

    // Lấy danh sách lớp học theo department
    public List<ClassEntity> getClassesByDepartment(Long departmentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa: " + departmentId));

        if (!"admin".equals(user.getRole().getName()) && !user.getDepartment().equals(department)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem lớp học của khoa này");
        }

        return classEntityRepository.findByDepartments_Id(departmentId);
    }

    // Lấy danh sách lớp học theo major
    public List<ClassEntity> getClassesByMajor(Long majorId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành: " + majorId));

        if (!"admin".equals(user.getRole().getName()) && !user.getMajor().equals(major)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem lớp học của ngành này");
        }

        return classEntityRepository.findByMajors_Id(majorId);
    }

    // Thay đổi visibility của lớp học
    @Transactional
    public ClassEntity changeVisibility(Long classId, ClassEntity.VisibilityScope visibility, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền thay đổi phạm vi của lớp học này");
        }

        classEntity.setVisibility(visibility);
        return classEntityRepository.save(classEntity);
    }

    // Thêm môn học vào lớp
    @Transactional
    public void addSubjectToClass(Long classId, Long subjectId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền thêm môn học vào lớp này");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));

        // Kiểm tra môn học có thuộc cùng khoa/ngành không
        if (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT) {
            boolean isSubjectInDepartment = subject.getDepartments().stream()
                    .anyMatch(department -> classEntity.getDepartments().contains(department));
            if (!isSubjectInDepartment) {
                throw new UnauthorizedAccessException("Môn học không thuộc cùng khoa với lớp học");
            }
        } else if (classEntity.getVisibility() == ClassEntity.VisibilityScope.MAJOR) {
            boolean isSubjectInMajor = subject.getMajors().stream()
                    .anyMatch(major -> classEntity.getMajors().contains(major));
            if (!isSubjectInMajor) {
                throw new UnauthorizedAccessException("Môn học không thuộc cùng ngành với lớp học");
            }
        }

        // Kiểm tra môn học đã tồn tại trong lớp chưa
        if (classSubjectRepository.existsByClassEntityAndSubject(classEntity, subject)) {
            throw new RuntimeException("Môn học đã tồn tại trong lớp");
        }

        // Tạo mối quan hệ mới giữa lớp và môn học
        ClassSubject classSubject = new ClassSubject(classEntity, subject, null);
        classSubjectRepository.save(classSubject);
    }

    // Xóa môn học khỏi lớp
    @Transactional
    public void removeSubjectFromClass(Long classId, Long subjectId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xóa môn học khỏi lớp này");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));

        Optional<ClassSubject> classSubjectOpt = Optional.ofNullable(classSubjectRepository.findByClassEntityAndSubject(classEntity, subject));
        ClassSubject classSubject = classSubjectOpt
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại trong lớp"));

        classSubjectRepository.delete(classSubject);
    }

    // Lấy danh sách môn học trong lớp
    public List<Subject> getSubjectsInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        return classSubjectRepository.findByClassEntity(classEntity).stream()
                .map(ClassSubject::getSubject)
                .collect(Collectors.toList());
    }

    // Lấy danh sách lớp học theo môn học
    public List<ClassEntity> getClassesBySubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));

        return classSubjectRepository.findBySubject(subject).stream()
                .map(ClassSubject::getClassEntity)
                .collect(Collectors.toList());
    }

    public List<ClassSubject> getDocumentsInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        return classSubjectRepository.findByClassEntity(classEntity).stream()
                .filter(classSubject -> classSubject.getDocumentUrl() != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocumentFromClass(Long classId, Long documentId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền xóa tài liệu khỏi lớp này");
        }

        ClassSubject classSubject = classSubjectRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại"));

        if (!classSubject.getClassEntity().equals(classEntity)) {
            throw new UnauthorizedAccessException("Tài liệu không thuộc về lớp học này");
        }

        // Xóa file trên Google Drive
        try {
            googleDriveService.deleteFile(classSubject.getDocumentUrl());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file trên Google Drive: " + e.getMessage());
        }

        // Xóa URL trong database
        classSubject.setDocumentUrl(null);
        classSubjectRepository.save(classSubject);
    }

    public List<JoinRequest> getJoinRequestsInClass(Long classId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        return classEntity.getJoinRequests().stream()
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveJoinRequest(Long classId, Long requestId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền phê duyệt yêu cầu tham gia lớp này");
        }

        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu tham gia không tồn tại"));

        if (!joinRequest.getClassEntity().equals(classEntity)) {
            throw new UnauthorizedAccessException("Yêu cầu tham gia không thuộc về lớp học này");
        }

        if (joinRequest.getType() == JoinRequest.Type.STUDENT_REQUEST) {
            classEntity.getUsers().add(joinRequest.getUser());
        } else if (joinRequest.getType() == JoinRequest.Type.TEACHER_INVITE) {
            classEntity.getTeachers().add(joinRequest.getUser());
        }

        joinRequest.setStatus(JoinRequest.Status.APPROVED);
        joinRequestRepository.save(joinRequest);
        classEntityRepository.save(classEntity);
    }

    @Transactional
    public void rejectJoinRequest(Long classId, Long requestId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));

        if (!isAuthorized(userId, classEntity)) {
            throw new UnauthorizedAccessException("Bạn không có quyền từ chối yêu cầu tham gia lớp này");
        }

        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu tham gia không tồn tại"));

        if (!joinRequest.getClassEntity().equals(classEntity)) {
            throw new UnauthorizedAccessException("Yêu cầu tham gia không thuộc về lớp học này");
        }

        joinRequest.setStatus(JoinRequest.Status.REJECTED);
        joinRequestRepository.save(joinRequest);
    }
}
