package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Model.User;
import com.example.vjutest.Service.UserService;

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestParam Long userId) {
        try {
            user.setImage("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg"); 
            User createdUser = userService.createUser(user, userId);
            return ResponseEntity.ok(userMapper.toDTO(createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user, @RequestParam Long userId) {
        try {
            User updatedUser = userService.updateUser(id, user, userId);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam Long userId) {
        try {
            userService.deleteUser(id, userId);
            return ResponseEntity.ok("Xóa người dùng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllSubjects(@RequestParam Long userId) {
        List<User> users = userService.getAllUsers(userId);
        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/find/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id, @RequestParam Long userId) {
        return userService.getUserById(id, userId)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam Long userId) {
        try {
            User user = userService.getProfile(userId);
            return ResponseEntity.ok(userMapper.toDTO(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@RequestParam Long userId, @RequestBody User user) {
        try {
            User updatedUser = userService.updateProfile(userId, user);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}