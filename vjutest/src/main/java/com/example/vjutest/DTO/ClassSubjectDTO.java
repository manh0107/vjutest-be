package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassSubjectDTO {
    private Long id;
    private ClassEntityDTO classEntity;
    private SubjectDTO subject;
    private String documentUrl;
}
