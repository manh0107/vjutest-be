package com.example.vjutest.Exception;

public class DuplicateDepartmentNameException extends RuntimeException {
    public DuplicateDepartmentNameException(String message) {
        super(message);
    }
} 