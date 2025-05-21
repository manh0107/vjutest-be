package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassSubjectDTO {
    private Long id;
    private ClassEntityDTO classEntity;
    private SubjectDTO subject;
    private String documentUrl;
}
