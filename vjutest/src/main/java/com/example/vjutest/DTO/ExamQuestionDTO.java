package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamQuestionDTO {
    private Long id;
    private String examName;
    private Long examId;
    private String subjectName;
    private Long subjectId;
    private Long questionId;
    private Integer point;

    private ExamDTO exam;
}
