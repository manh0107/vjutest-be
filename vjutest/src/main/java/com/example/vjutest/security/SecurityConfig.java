package com.example.vjutest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép tất cả truy cập mà không cần đăng nhập
            )
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để tránh lỗi với Postman
            .formLogin(form -> form.disable()) // Tắt form đăng nhập
            .httpBasic(httpBasic -> httpBasic.disable()); // Tắt Basic Auth (nếu không cần)

        return http.build();
    }
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
