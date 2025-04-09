package com.example.vjutest.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private Long code;
    private Long phoneNumber;
    private String className;
    private String gender;
    private String email;
    private String image;
    private String role;
    
    @JsonProperty("isEnabled")
    private Boolean isEnabled;

    private List<Long> createClasses;
    private List<Long> createSubjects;
    private List<Long> createdExams;
    private List<Long> createdQuestions;
    private List<Long> classes;
    private List<Long> teacherOfClasses;
    private List<Long> joinRequests;
    private List<Long> userAnswers;
}
