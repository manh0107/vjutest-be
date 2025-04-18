package com.example.vjutest.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAnswerDTO {

    private Long id;
    private Long userId;
    private Long questionId;
    private Long answerId;
    private Long examId;
    
    @JsonProperty("isSubmitted")
    private Boolean isSubmitted = false;

    private ResultDTO result;
}
