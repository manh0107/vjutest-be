package com.example.vjutest.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectDTO {
    private Long id;
    private String name;
    private String subjectCode;
    private String description;
    private Integer creditHour;
    private Long createdBy;
    private String createdByName;
    private List<ExamDTO> exams; 
}
