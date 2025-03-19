package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectDTO {
    private Long id;
    private String name;
    private String subjectCode;
    private String description;
    private Integer creditHour;
    private Long createdBy; 
}
