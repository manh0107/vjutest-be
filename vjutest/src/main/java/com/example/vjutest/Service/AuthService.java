package com.example.vjutest.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vjutest.DTO.AuthRequest;
import com.example.vjutest.DTO.AuthResponse;
import com.example.vjutest.DTO.RegisterRequest;
import com.example.vjutest.Jwt.JwtTokenProvider;
import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.User.CustomUserDetails;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    // 🔹 Tạo mã xác nhận ngẫu nhiên gồm 6 chữ số
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Mã 6 chữ số từ 100000 đến 999999
        return String.valueOf(code);
    }

    // 🔹 ĐĂNG KÝ
    public String register(RegisterRequest request) {
        Optional<Role> roleOpt = roleRepository.findByName(request.getRoleName());
        if (roleOpt.isEmpty()) {
            return "Role không tồn tại!";
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email đã tồn tại!";
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String verificationCode = generateVerificationCode();

        User user = new User(
                request.getName(),
                request.getCode(),
                request.getPhoneNumber(),
                request.getClassName(),
                request.getGender(),
                request.getEmail(),
                encodedPassword,
                roleOpt.get(),
                "https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg"
        );

        user.setVerificationToken(verificationCode);  // Lưu mã xác nhận
        user.setEnabled(false);  // Mặc định chưa kích hoạt tài khoản
        userRepository.save(user);

        // Gửi mã xác nhận qua email
        emailService.sendEmail(user.getEmail(), "Mã xác nhận tài khoản", 
                "Mã xác nhận của bạn là: " + verificationCode);

        return "Đăng ký thành công! Kiểm tra email để lấy mã xác nhận.";
    }

    // 🔹 XÁC NHẬN EMAIL
    public String verifyEmail(String email, String verificationCode) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Email không tồn tại!";
        }

        User user = userOpt.get();

        // Kiểm tra mã xác nhận có khớp không
        if (!user.getVerificationToken().equals(verificationCode)) {
            return "Mã xác nhận không hợp lệ!";
        }

        // Kích hoạt tài khoản và xóa mã xác nhận
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Tài khoản đã được kích hoạt thành công!";
    }

    // 🔹 ĐĂNG NHẬP
    public AuthResponse login(AuthRequest request) {
        try {
            System.out.println("Đang xác thực user: " + request.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            // Lấy thông tin người dùng từ Authentication
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User userEntity = userDetails.getUser();
    
            // Kiểm tra tài khoản đã kích hoạt hay chưa
            if (!userEntity.isEnabled()) {
                return new AuthResponse(null, "Tài khoản chưa kích hoạt!");
            }
    
            // Tạo JWT token
            String jwt = tokenProvider.generateToken(userDetails);
            return new AuthResponse(jwt, "Đăng nhập thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi xác thực: " + e.getMessage());
            throw new RuntimeException("Đăng nhập thất bại!");
        }
    }

    // 🔹 QUÊN MẬT KHẨU
    public String forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Email không tồn tại!";
        }

        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setVerificationToken(resetToken);
        userRepository.save(user);

        // Gửi email reset mật khẩu
        String link = "http://localhost:8080/auth/reset-password?token=" + resetToken;
        emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", "Bấm vào đây để đặt lại mật khẩu: " + link);

        return "Hãy kiểm tra email để đặt lại mật khẩu.";
    }

    // 🔹 ĐẶT LẠI MẬT KHẨU
    public String resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return "Mã không hợp lệ!";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Mật khẩu đã được đặt lại!";
    }
}
