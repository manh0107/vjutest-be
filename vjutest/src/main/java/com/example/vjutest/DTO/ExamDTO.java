package com.example.vjutest.DTO;

import java.time.LocalDateTime;

import com.example.vjutest.Model.Exam.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamDTO {
    private Long id;
    private String name;
    private String examCode;
    private String description;
    private Long durationTime;
    private Integer maxScore;
    private Status status;

    @JsonProperty("isPublic")
    private Boolean isPublic;
    
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Long createdBy;
    private String createdByName;

    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private ClassSubjectDTO classSubject;
    private SubjectDTO subject;
}
