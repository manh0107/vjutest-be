package com.example.vjutest.DTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private Long code;
    private Long phoneNumber;
    private String className;
    private String gender;
    private String email;
    private String image;
    private Long role;

    private List<Long> createClasses;
    private List<Long> createSubjects;
    private List<Long> createdExams;
    private List<Long> createdQuestions;
    private List<Long> classes;
    private List<Long> userAnswer;
}
