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
public class ResultDTO {
    public Long id;
    public Long examId;
    public String examName;
    public Long userId;
    public String userName;
    public Integer score;
    public Long durationTime;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public LocalDateTime submittedAt;

    public String passTest;
    
    @JsonProperty("isSubmitted")
    public Boolean isSubmitted;

    public UserDTO user;
    public ExamDTO exam;
}
