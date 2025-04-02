package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private Long code;
    private Long phoneNumber;
    private String className;
    private String gender;
    private String email;
    private String password;
    private String roleName;  // "student" hoặc "teacher"
}
