package com.example.vjutest.DTO;

import java.time.LocalDateTime;
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
public class QuestionDTO {
    private Long id;
    private String name;
    private Integer difficulty;

    @JsonProperty("isPublic")
    private Boolean isPublic;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Long createdById;
    private String createdByName;

    private Long modifiedById;
    private String modifiedByName;

    private Long chapterId;
    private String chapterName;

    private ChapterDTO chapter;

    private List<ExamQuestionDTO> examQuestions;

    private String imageUrl;
}
