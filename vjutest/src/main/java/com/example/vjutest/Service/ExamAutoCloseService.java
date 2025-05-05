package com.example.vjutest.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Repository.ExamRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamAutoCloseService {

    private final ExamRepository examRepository;
    private final QuestionService questionService;

    // Chạy mỗi phút để kiểm tra và đóng các bài kiểm tra đã hết thời gian
    @Scheduled(fixedRate = 60000) // 60000 milliseconds = 1 minute
    @Transactional
    public void autoCloseExams() {
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy danh sách các bài kiểm tra đã hết thời gian nhưng chưa đóng
        List<Exam> expiredExams = examRepository.findByStatusAndEndAtBefore(
            Exam.Status.PUBLISHED,
            now
        );

        for (Exam exam : expiredExams) {
            // Đóng bài kiểm tra
            exam.setStatus(Exam.Status.CLOSED);
            examRepository.save(exam);

            // Tự động thêm câu hỏi public vào chương tương ứng
            questionService.autoAddPublicQuestionsToChapters(exam);
        }
    }
} 