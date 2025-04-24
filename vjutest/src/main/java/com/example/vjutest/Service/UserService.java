package com.example.vjutest.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Exception.ResourceNotFoundException;
import com.example.vjutest.Exception.UnauthorizedException;
import com.example.vjutest.Exception.ValidationException;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Repository.MajorRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, DepartmentRepository departmentRepository, MajorRepository majorRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.departmentRepository = departmentRepository;
        this.majorRepository = majorRepository;
    }

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
    
    public UserDTO updateUser(Long id, User updatedUser, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        checkAdminRole(currentUser);
    
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    
        if (updatedUser.getCode() != null && !updatedUser.getCode().equals(user.getCode())) {
            if (userRepository.existsByCodeAndIdNot(updatedUser.getCode(), userId)) {
                throw new ValidationException("Mã sinh viên đã được sử dụng");
            }
            user.setCode(updatedUser.getCode());
        }
    
        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumberAndIdNot(updatedUser.getPhoneNumber(), userId)) {
                throw new ValidationException("Số điện thoại đã được sử dụng");
            }
            user.setPhoneNumber(updatedUser.getPhoneNumber());
        }
    
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), userId)) {
                throw new ValidationException("Email đã được sử dụng");
            }
            user.setEmail(updatedUser.getEmail());
        }
    
        // Update department if provided
        if (updatedUser.getDepartment() != null && updatedUser.getDepartment().getId() != 0) {
            Department department = departmentRepository.findById(updatedUser.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + updatedUser.getDepartment().getId()));
            user.setDepartment(department);
        }

        // Update major if provided
        if (updatedUser.getMajor() != null && updatedUser.getMajor().getId() != 0) {
            Major major = majorRepository.findById(updatedUser.getMajor().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành với ID: " + updatedUser.getMajor().getId()));
            user.setMajor(major);
        }
    
        updateUserFields(user, updatedUser);
    
        if (updatedUser.getRole() != null && updatedUser.getRole().getId() != null) {
            Role role = roleRepository.findById(updatedUser.getRole().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò"));
            user.setRole(role);
        }
    
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    private void updateUserFields(User user, User updatedUser) {
        if (updatedUser.getPassword() != null) user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
        if (updatedUser.getImage() != null) user.setImage(updatedUser.getImage());
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
}