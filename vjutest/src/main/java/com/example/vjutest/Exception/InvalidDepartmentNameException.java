package com.example.vjutest.Exception;

public class InvalidDepartmentNameException extends RuntimeException {
    public InvalidDepartmentNameException(String message) {
        super(message);
    }
} 