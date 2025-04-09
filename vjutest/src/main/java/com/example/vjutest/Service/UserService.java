package com.example.vjutest.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private void checkAdminRole(User user) {
        if (!user.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ admin mới được phép thực hiện thao tác này!");
        }
    }

    public User createUser(User user, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        checkAdminRole(currentUser);

        // Validate required fields
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new RuntimeException("Tên không được để trống");
        }
    
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
    
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }
    
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (user.getCode() != null && userRepository.existsByCode(user.getCode())) {
            throw new RuntimeException("Mã số đã tồn tại");
        }
    
        if (user.getPhoneNumber() != null && userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + user.getRole().getId()));
    
        user.setRole(role);
        user.setIsEnabled(false); // Mặc định chưa kích hoạt
    
        return userRepository.save(user);
    }
    
    //Cập nhật người dùng
    public User updateUser(Long id, User updatedUser, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        checkAdminRole(currentUser);
    
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    
        // Check code trùng
        if (updatedUser.getCode() != null && !updatedUser.getCode().equals(user.getCode())) {
            if (userRepository.existsByCodeAndIdNot(updatedUser.getCode(), userId)) {
                throw new RuntimeException("Mã sinh viên đã được sử dụng");
            }
            user.setCode(updatedUser.getCode());
        }
    
        // Check phoneNumber trùng
        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumberAndIdNot(updatedUser.getPhoneNumber(), userId)) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }
            user.setPhoneNumber(updatedUser.getPhoneNumber());
        }
    
        // Check email trùng
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), userId)) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(updatedUser.getEmail());
        }
    
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getClassName() != null) user.setClassName(updatedUser.getClassName());
        if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
        if (updatedUser.getPassword() != null) user.setPassword(updatedUser.getPassword());
        if (updatedUser.getImage() != null) user.setImage(updatedUser.getImage());
        if (updatedUser.getIsEnabled() != null) user.setIsEnabled(updatedUser.getIsEnabled());
    
        if (updatedUser.getRole() != null && updatedUser.getRole().getId() != null) {
            Role role = roleRepository.findById(updatedUser.getRole().getId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
    
        return userRepository.save(user);
    }
    
    //Xóa người dùng
    public void deleteUser(Long deleteUserId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    
        checkAdminRole(user);

        User deleteUser = userRepository.findById(deleteUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        userRepository.delete(deleteUser);
    }

    //Lấy tất cả người dùng
    public List<User> getAllUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        checkAdminRole(user);

        return userRepository.findAll();
    }

    //Lấy người dùng theo id
    public Optional<User> getUserById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        checkAdminRole(user);
        return userRepository.findById(id);
    }

    // Lấy profile của chính người dùng đang đăng nhập
    public User getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if(user.getRole().getName().equalsIgnoreCase("student") || user.getRole().getName().equalsIgnoreCase("teacher")) {
            throw new RuntimeException("Chỉ có sinh viên và giáo viên mới có thể xem thông tin cá nhân!");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    // Cập nhật thông tin cá nhân (profile)
    public User updateProfile(Long userId, User updatedUser) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String roleName = currentUser.getRole().getName().toLowerCase();
        if (!roleName.equals("user") && !roleName.equals("teacher")) {
            throw new RuntimeException("Chỉ người dùng hoặc giáo viên mới được cập nhật hồ sơ của chính mình");
        }

        // Kiểm tra email trùng
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), userId)) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            currentUser.setEmail(updatedUser.getEmail());
        }

        // Kiểm tra mã số trùng
        if (updatedUser.getCode() != null && !updatedUser.getCode().equals(currentUser.getCode())) {
            if (userRepository.existsByCodeAndIdNot(updatedUser.getCode(), userId)) {
                throw new RuntimeException("Mã số đã được sử dụng");
            }
            currentUser.setCode(updatedUser.getCode());
        }

        // Kiểm tra số điện thoại trùng
        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumberAndIdNot(updatedUser.getPhoneNumber(), userId)) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        // Cập nhật các trường khác
        if (updatedUser.getName() != null) currentUser.setName(updatedUser.getName());
        if (updatedUser.getClassName() != null) currentUser.setClassName(updatedUser.getClassName());
        if (updatedUser.getGender() != null) currentUser.setGender(updatedUser.getGender());
        if (updatedUser.getPassword() != null) currentUser.setPassword(updatedUser.getPassword());
        if (updatedUser.getImage() != null) currentUser.setImage(updatedUser.getImage());

        // Không cho cập nhật role hoặc isEnabled
        return userRepository.save(currentUser);
    }
}