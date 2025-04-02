package com.example.vjutest.Jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.vjutest.Service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        System.out.println("JWT Filter đang chạy cho request: " + request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtTokenProvider.extractEmail(jwt);

        if (userEmail == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (SecurityContextHolder.getContext().getAuthentication() == null &&
                jwtTokenProvider.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Xác thực thành công cho: " + userEmail);
            }
        } catch (Exception e) {
            System.out.println("Lỗi xác thực JWT: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }
}