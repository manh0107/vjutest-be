package com.example.vjutest.DTO;

import java.time.LocalDateTime;

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
public class AnswerDTO {
    private Long id;
    private String answerName;

    @JsonProperty("isCorrect")
    private Boolean isCorrect;

    private Long questionId;
    private String questionName;
    private Long createdById;
    private String createdByName;
    private Long modifiedById;
    private String modifiedByName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private QuestionDTO question;
    private UserDTO user;

    private String imageUrl;

    private Double percentChosen;
}
