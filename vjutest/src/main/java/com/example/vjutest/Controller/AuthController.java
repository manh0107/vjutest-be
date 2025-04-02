package com.example.vjutest.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.AuthRequest;
import com.example.vjutest.DTO.AuthResponse;
import com.example.vjutest.DTO.RegisterRequest;
import com.example.vjutest.Service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(email, request.getVerifyEmailCode()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }

    // Create this class in your project
    public static class VerificationRequest {
        private String verifyEmailCode;
        
        // Getters and setters
        public String getVerifyEmailCode() {
            return verifyEmailCode;
        }
        
        public void setVerifyEmailCode(String verifyEmailCode) {
            this.verifyEmailCode = verifyEmailCode;
        }
    }
}
