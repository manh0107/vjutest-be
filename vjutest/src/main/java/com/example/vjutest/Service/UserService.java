package com.example.vjutest.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Mapper.ClassEntityMapper;
import com.example.vjutest.Mapper.ExamMapper;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Exception.ResourceNotFoundException;
import com.example.vjutest.Exception.UnauthorizedException;
import com.example.vjutest.Exception.ValidationException;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Question;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final ClassEntityMapper classEntityMapper;
    private final ExamMapper examMapper;
    private final QuestionMapper questionMapper;
    private final CloudinaryService cloudinaryService;

    private void checkAdminRole(User user) {
        if (!user.getRole().getName().equalsIgnoreCase("admin")) {
            throw new UnauthorizedException("Chỉ admin mới được phép thực hiện thao tác này!");
        }
    }

    private void validateUserFields(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new ValidationException("Tên không được để trống");
        }
    
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email không được để trống");
        }
    
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống");
        }
    }

    public UserDTO createUser(User user, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        checkAdminRole(currentUser);
        validateUserFields(user);

        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new ValidationException("Email đã tồn tại");
        }

        if (user.getCode() != null && userRepository.existsByCode(user.getCode())) {
            throw new ValidationException("Mã số đã tồn tại");
        }
    
        if (user.getPhoneNumber() != null && userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new ValidationException("Số điện thoại đã tồn tại");
        }

        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + user.getRole().getId()));
    
        user.setRole(role);
        user.setIsEnabled(user.getIsEnabled());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    
        // Set department and major if provided
        if (user.getDepartment() != null && user.getDepartment().getId() != 0) {
            Department department = departmentRepository.findById(user.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + user.getDepartment().getId()));
            user.setDepartment(department);
        }

        if (user.getMajor() != null && user.getMajor().getId() != 0) {
            Major major = majorRepository.findById(user.getMajor().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành với ID: " + user.getMajor().getId()));
            user.setMajor(major);
        }
    
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(Long id, User updatedUser, Long userId, MultipartFile imageFile) throws IOException {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        checkAdminRole(currentUser);
    
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    
        // Log để debug
        System.out.println("Current user data: " + user);
        System.out.println("Updated user data: " + updatedUser);
    
        // Nếu updatedUser là null, chỉ update ảnh
        if (updatedUser != null) {
            // Update các trường cơ bản
            if (updatedUser.getName() != null) user.setName(updatedUser.getName());
            if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
            if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
            if (updatedUser.getIsEnabled() != null) user.setIsEnabled(updatedUser.getIsEnabled());
            if (updatedUser.getPassword() != null) user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

            // Update code nếu có thay đổi
            if (updatedUser.getCode() != null && !updatedUser.getCode().equals(user.getCode())) {
                if (userRepository.existsByCodeAndIdNot(updatedUser.getCode(), id)) {
                    throw new ValidationException("Mã sinh viên đã được sử dụng");
                }
                user.setCode(updatedUser.getCode());
            }
    
            // Update phone number nếu có thay đổi
            if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumberAndIdNot(updatedUser.getPhoneNumber(), id)) {
                    throw new ValidationException("Số điện thoại đã được sử dụng");
                }
                user.setPhoneNumber(updatedUser.getPhoneNumber());
            }
    
            // Update department nếu có thay đổi
            if (updatedUser.getDepartment() != null && updatedUser.getDepartment().getId() != 0) {
                Department department = departmentRepository.findById(updatedUser.getDepartment().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + updatedUser.getDepartment().getId()));
                user.setDepartment(department);
            }

            // Update major nếu có thay đổi
            if (updatedUser.getMajor() != null && updatedUser.getMajor().getId() != 0) {
                Major major = majorRepository.findById(updatedUser.getMajor().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành với ID: " + updatedUser.getMajor().getId()));
                user.setMajor(major);
            }

            // Update role nếu có thay đổi
            if (updatedUser.getRole() != null && updatedUser.getRole().getId() != null) {
                Role role = roleRepository.findById(updatedUser.getRole().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò"));
                user.setRole(role);
            }
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ValidationException("Chỉ được phép tải lên file ảnh!");
            }

            // Delete old image if exists and not default image
            if (user.getImageUrl() != null && !user.getImageUrl().equals("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg")) {
                cloudinaryService.deleteImage(user.getImageUrl());
            }
            // Upload new image
            String imageUrl = cloudinaryService.uploadImage(imageFile, "users");
            user.setImageUrl(imageUrl);
        }

        user.setModifiedBy(currentUser);
        user.setModifiedAt(LocalDateTime.now());
    
        // Save and flush to ensure changes are committed immediately
        User savedUser = userRepository.saveAndFlush(user);
        userRepository.flush(); // Force flush to database

        // Log để debug
        System.out.println("Saved user data: " + savedUser);

        return userMapper.toDTO(savedUser);
    }

    private void updateUserFields(User user, User updatedUser) {
        if (updatedUser.getPassword() != null) user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
        if (updatedUser.getImageUrl() != null) user.setImageUrl(updatedUser.getImageUrl());
        if (updatedUser.getIsEnabled() != null) user.setIsEnabled(updatedUser.getIsEnabled());
    }
    
    public void deleteUser(Long deleteUserId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    
        checkAdminRole(user);

        User deleteUser = userRepository.findById(deleteUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        userRepository.delete(deleteUser);
    }

    public List<UserDTO> getAllUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        
        checkAdminRole(user);

        return userRepository.findAll().stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id, Long userId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return userMapper.toDTO(user);
    }

    public UserDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        
        String roleName = user.getRole().getName().toLowerCase();
        if (!roleName.equals("student") && !roleName.equals("teacher")) {
            throw new UnauthorizedException("Chỉ có sinh viên và giáo viên mới có thể xem thông tin cá nhân!");
        }
        
        return userMapper.toDTO(user);
    }

    public UserDTO updateProfile(Long userId, User updatedUser) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        String roleName = currentUser.getRole().getName().toLowerCase();
        if (!roleName.equals("student") && !roleName.equals("teacher")) {
            throw new UnauthorizedException("Chỉ người dùng hoặc giáo viên mới được cập nhật hồ sơ của chính mình");
        }

        validateProfileUpdate(currentUser, updatedUser);
        updateUserFields(currentUser, updatedUser);

        // Update department if provided
        if (updatedUser.getDepartment() != null && updatedUser.getDepartment().getId() != 0) {
            Department department = departmentRepository.findById(updatedUser.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + updatedUser.getDepartment().getId()));
            currentUser.setDepartment(department);
        }

        // Update major if provided
        if (updatedUser.getMajor() != null && updatedUser.getMajor().getId() != 0) {
            Major major = majorRepository.findById(updatedUser.getMajor().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành với ID: " + updatedUser.getMajor().getId()));
            currentUser.setMajor(major);
        }

        User savedUser = userRepository.save(currentUser);
        return userMapper.toDTO(savedUser);
    }

    private void validateProfileUpdate(User currentUser, User updatedUser) {
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), currentUser.getId())) {
                throw new ValidationException("Email đã được sử dụng");
            }
        }

        if (updatedUser.getCode() != null && !updatedUser.getCode().equals(currentUser.getCode())) {
            if (userRepository.existsByCodeAndIdNot(updatedUser.getCode(), currentUser.getId())) {
                throw new ValidationException("Mã số đã được sử dụng");
            }
        }

        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumberAndIdNot(updatedUser.getPhoneNumber(), currentUser.getId())) {
                throw new ValidationException("Số điện thoại đã được sử dụng");
            }
        }
    }

    public List<ClassEntityDTO> getUserCreatedClasses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        List<ClassEntity> createdClasses = new ArrayList<>(user.getCreateClasses());
        return createdClasses.stream()
                .map(classEntityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExamDTO> getUserCreatedExams(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        List<Exam> createdExams = new ArrayList<>(user.getCreatedExams());
        return createdExams.stream()
                .map(examMapper::toSimpleDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getUserCreatedQuestions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        List<Question> createdQuestions = new ArrayList<>(user.getCreatedQuestions());
        return createdQuestions.stream()
                .map(questionMapper::toSimpleDTO)
                .collect(Collectors.toList());
    }

    public List<ClassEntityDTO> getTeacherClasses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!user.getRole().getName().equalsIgnoreCase("ROLE_TEACHER") && !user.getRole().getName().equalsIgnoreCase("ROLE_ADMIN")) {
            throw new UnauthorizedException("Chỉ giáo viên mới có thể xem danh sách lớp dạy");
        }

        List<ClassEntity> teachingClasses = new ArrayList<>(user.getTeacherOfClasses());
        return teachingClasses.stream()
                .map(classEntityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public long countAllUsers() {
        return userRepository.count();
    }

}