package com.example.vjutest.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class ExamStatisticsDTO {
    private Long examId;
    private String examName;
    private int totalStudents;
    private int submittedCount;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private Map<String, Integer> gradeDistribution; // A, B, C, D, F
    private Map<Double, Integer> scoreDistribution; // Score ranges and counts
} 