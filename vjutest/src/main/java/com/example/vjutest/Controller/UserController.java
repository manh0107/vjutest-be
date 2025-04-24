package com.example.vjutest.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Model.User;
import com.example.vjutest.Service.UserService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.Mapper.UserMapper;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // Add this if you're calling from a different origin
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // Chỉ admin có quyền tạo người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            user.setImage("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg"); 
            UserDTO createdUser = userService.createUser(user, userId);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật thông tin người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            UserDTO updatedUser = userService.updateUser(id, user, userId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            userService.deleteUser(id, userId);
            return ResponseEntity.ok("Xóa người dùng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy tất cả người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  
        List<UserDTO> users = userService.getAllUsers(userId);
        return ResponseEntity.ok(users);
    }

    // Lấy thông tin người dùng theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/find/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            UserDTO userDTO = userService.getUserById(id, userId);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy thông tin cá nhân của người dùng (ROLE_USER hoặc ROLE_TEACHER)
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            UserDTO user = userService.getProfile(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật thông tin cá nhân
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO userDTO, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            User user = userMapper.toEntity(userDTO);
            UserDTO updatedUser = userService.updateProfile(userId, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}