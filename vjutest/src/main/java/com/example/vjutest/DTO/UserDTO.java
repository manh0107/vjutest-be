package com.example.vjutest.DTO;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Long id;
    private String name;
    private Long code;
    private Long phoneNumber;
    private String gender;
    private String email;
    private String imageUrl;
    private String role;

    private DepartmentDTO department;
    private MajorDTO major;
    
    @JsonProperty("isEnabled")
    private Boolean isEnabled;

    private List<Long> createDepartments;
    private List<Long> createMajors;
    private List<Long> createClasses;
    private List<Long> createSubjects;
    private List<Long> createdExams;
    private List<Long> createdQuestions;
    private List<Long> classes;
    private List<Long> teacherOfClasses;
    private List<Long> joinRequests;
    private List<Long> userAnswers;
    private LocalDateTime createdAt;
}
