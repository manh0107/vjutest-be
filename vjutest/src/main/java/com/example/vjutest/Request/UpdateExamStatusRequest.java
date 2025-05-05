package com.example.vjutest.Request;

import java.time.LocalDateTime;

import com.example.vjutest.Model.Exam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamStatusRequest {
    private Exam.Status newStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer passPercent;
    private Long durationTime;
}