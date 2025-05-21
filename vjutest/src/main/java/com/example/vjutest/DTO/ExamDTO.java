package com.example.vjutest.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.example.vjutest.Model.Exam.ExamVisibility;
import com.example.vjutest.Model.Exam.Status;
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
public class ExamDTO {
    private Long id;
    private String name;
    private String examCode;
    private String description;
    private Long durationTime;
    private Integer passScore;
    private Integer maxScore;
    private Integer passPercent;
    private Integer questionsCount;
    private Status status;

    @JsonProperty("isPublic")
    private Boolean isPublic;

    private ExamVisibility visibility;
    
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Long createdById;
    private String createdByName;

    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private ClassSubjectDTO classSubject;
    private SubjectDTO subject;
    private UserDTO user;

    private List<Long> departmentIds;
    private List<Long> majorIds;
    private List<Long> selectedDepartments;
    private List<Long> selectedMajors;
    
    private Integer maxAttempts;
    private Boolean randomQuestions;
    
    @JsonProperty("markedAsPublic")
    private Boolean markedAsPublic;

    private List<Long> chapterIds;
}
