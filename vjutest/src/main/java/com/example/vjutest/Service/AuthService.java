package com.example.vjutest.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.vjutest.DTO.AuthRequest;
import com.example.vjutest.DTO.AuthResponse;
import com.example.vjutest.DTO.RegisterRequest;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Jwt.JwtTokenProvider;
import com.example.vjutest.Mapper.UserMapper;
import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.Token;
import com.example.vjutest.Model.Token.TokenType;
// Ensure no conflicting imports for Token class
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.TokenRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.User.CustomUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserMapper userMapper;

    //Tạo mã xác nhận ngẫu nhiên gồm 6 chữ số
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Mã 6 chữ số từ 100000 đến 999999
        return String.valueOf(code);
    }

    //ĐĂNG KÝ
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
                request.getGender(),
                request.getEmail(),
                encodedPassword,
                roleOpt.get(),
                "https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg",
                false
        );

        user.setVerificationToken(verificationCode);  // Lưu mã xác nhận
        userRepository.save(user);

        // Gửi mã xác nhận qua email
        emailService.sendEmail(user.getEmail(), "Mã xác nhận tài khoản", 
                "Mã xác nhận của bạn là: " + verificationCode);

        return "Đăng ký thành công! Kiểm tra email để lấy mã xác nhận.";
    }

    //XÁC NHẬN EMAIL
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
        user.setIsEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Tài khoản đã được kích hoạt thành công!";
    }

    //ĐĂNG NHẬP
    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        try {
            System.out.println("Đang xác thực user: " + request.getEmail());

            // Xác thực thông tin đăng nhập
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // Lấy thông tin user từ kết quả xác thực
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User userEntity = userDetails.getUser();

            // Kiểm tra tài khoản đã kích hoạt chưa
            if (Boolean.FALSE.equals(userEntity.getIsEnabled())) {
                throw new RuntimeException("Tài khoản chưa được kích hoạt! Vui lòng kiểm tra email.");
            }

            // Tạo access & refresh token
            String accessToken = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Lưu refresh token vào DB
            revokeAllUserTokens(userEntity);
            saveUserToken(userEntity, refreshToken, TokenType.REFRESH);
            saveUserToken(userEntity, accessToken, TokenType.ACCESS);

            // Đặt refresh token vào cookie
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/"); // Toàn hệ thống
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            response.addCookie(cookie);

            return new AuthResponse(accessToken, "Đăng nhập thành công!");

        } catch (Exception e) {
            System.out.println("Lỗi xác thực: " + e.getMessage());
            throw new RuntimeException("Đăng nhập thất bại!");
        }
    }

    //QUÊN MẬT KHẨU
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

    //ĐẶT LẠI MẬT KHẨU
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

    //Đăng xuất
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Lấy access token từ header (Authorization)
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7); // Lấy token sau "Bearer "
        }

        // Lấy refresh token từ cookie
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    cookie.setMaxAge(0);  // Xóa refresh token cookie
                    cookie.setPath("/"); // Đảm bảo xóa trên tất cả các path
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        // Xóa access token trong DB
        if (accessToken != null) {
            Optional<Token> optionalToken = tokenRepository.findByToken(accessToken);
            if (optionalToken.isPresent()) {
                Token token = optionalToken.get();
                token.setIsRevoked(true); // Đánh dấu là revoked
                token.setIsExpired(true); // Đánh dấu là expired (nếu muốn)
                tokenRepository.save(token);
            }
        }

        // Xóa refresh token trong DB
        if (refreshToken != null) {
            Optional<Token> optionalRefreshTokenEntity = tokenRepository.findByToken(refreshToken);
            if (optionalRefreshTokenEntity.isPresent()) {
                Token refreshTokenEntity = optionalRefreshTokenEntity.get();
                refreshTokenEntity.setIsRevoked(true); // Đánh dấu là revoked
                refreshTokenEntity.setIsExpired(true); // Đánh dấu là expired (nếu muốn)
                tokenRepository.save(refreshTokenEntity);
            }
        }
    }


    //Lấy lại access token
    public AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new RuntimeException("Không tìm thấy refresh token");

        String refreshToken = Arrays.stream(cookies)
            .filter(c -> "refreshToken".equals(c.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);

        if (refreshToken == null) throw new RuntimeException("Refresh token không hợp lệ");

        String email = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (jwtTokenProvider.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        Optional<Token> tokenOpt = tokenRepository.findByToken(refreshToken);
        if (tokenOpt.isEmpty() || tokenOpt.get().getIsRevoked()) {
            throw new RuntimeException("Refresh token đã bị thu hồi hoặc không hợp lệ");
        }

        // Tạo access token mới
        UserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);

        // Xoá các access token cũ
        List<Token> oldTokens = tokenRepository.findAllByUser(user).stream()
            .filter(token -> token.getTokenType() == TokenType.ACCESS)
            .toList();
        tokenRepository.deleteAll(oldTokens);

        // Lưu access token mới
        saveUserToken(user, newAccessToken, TokenType.ACCESS);

        return new AuthResponse(newAccessToken, "Access token mới được cấp");
    }

    private void revokeAllUserTokens(User user) {
        List<Token> tokens = tokenRepository.findAllByUser(user);
        for (Token token : tokens) {
            token.setIsRevoked(true);
            token.setIsExpired(true);
        }
        tokenRepository.saveAll(tokens);
    }

    public void saveUserToken(User user, String token, TokenType tokenType) {
        Token newToken = new Token();
        newToken.setToken(token);
        newToken.setIsRevoked(false);
        newToken.setIsExpired(false);
        newToken.setTokenType(tokenType);
        newToken.setUser(user);
        tokenRepository.save(newToken);
    }

    //Lấy thông tin người dùng khi đăng nhập thành công
    public UserDTO getUserInfo(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa đăng nhập!");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Sử dụng phương thức với collections để lấy đầy đủ thông tin
        User userWithCollections = userRepository.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        return userMapper.toDTO(userWithCollections);
    }
}
