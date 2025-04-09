package com.example.vjutest.DTO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    private Long id;
    private String name;
    private Integer difficulty;

    @JsonProperty("isPublic")
    private Boolean isPublic;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Long createdBy;
    private String createdByName;

    private Long modifiedBy;

    private Long subjectId;
    private String subjectName;

    private SubjectDTO subject;

    private ExamQuestionDTO examQuestions;
}
