package com.example.vjutest.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Model.User;
import com.example.vjutest.Service.UserService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Service.CloudinaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // Add this if you're calling from a different origin
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
    }

    // Chỉ admin có quyền tạo người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();  // Lấy userId từ Authentication
            user.setImageUrl("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg"); 
            UserDTO createdUser = userService.createUser(user, userId);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật thông tin người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                      @RequestPart(value = "user", required = false) String userJson,
                                      @RequestPart(value = "file", required = false) MultipartFile file,
                                      Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            User user = null;
            if (userJson != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    user = objectMapper.readValue(userJson, User.class);
                    // Log để debug
                    System.out.println("Parsed user data: " + user);
                } catch (Exception e) {
                    System.err.println("Error parsing user JSON: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.badRequest().body("Lỗi khi parse dữ liệu người dùng: " + e.getMessage());
                }
            }
            UserDTO updatedUser = userService.updateUser(id, user, userId, file);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
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

    // Lấy danh sách lớp học đã tạo
    @GetMapping("/{userId}/created-classes")
    public ResponseEntity<List<ClassEntityDTO>> getUserCreatedClasses(@PathVariable Long userId) {
        try {
            List<ClassEntityDTO> classes = userService.getUserCreatedClasses(userId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách bài kiểm tra đã tạo
    @GetMapping("/{userId}/created-exams")
    public ResponseEntity<List<ExamDTO>> getUserCreatedExams(@PathVariable Long userId) {
        try {
            List<ExamDTO> exams = userService.getUserCreatedExams(userId);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách câu hỏi đã tạo
    @GetMapping("/{userId}/created-questions")
    public ResponseEntity<List<QuestionDTO>> getUserCreatedQuestions(@PathVariable Long userId) {
        try {
            List<QuestionDTO> questions = userService.getUserCreatedQuestions(userId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách lớp học đang dạy (chỉ dành cho giáo viên)
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/{userId}/teaching-classes")
    public ResponseEntity<List<ClassEntityDTO>> getTeacherClasses(@PathVariable Long userId) {
        try {
            List<ClassEntityDTO> classes = userService.getTeacherClasses(userId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, "users");
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }
}