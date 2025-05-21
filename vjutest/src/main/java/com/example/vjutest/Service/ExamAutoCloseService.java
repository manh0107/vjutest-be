package com.example.vjutest.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Repository.ExamRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamAutoCloseService {
    private static final Logger logger = LoggerFactory.getLogger(ExamAutoCloseService.class);
    
    private final ExamRepository examRepository;
    private final QuestionService questionService;
    private final ExamService examService;

    // Chạy mỗi phút để kiểm tra và đóng các bài kiểm tra đã hết thời gian
    @Scheduled(fixedRate = 60000) // 60000 milliseconds = 1 minute
    @Transactional
    public void autoCloseExams() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Running autoCloseExams at: {}", now);
        
        // Lấy danh sách các bài kiểm tra đã hết thời gian nhưng chưa đóng
        List<Exam> expiredExams = examRepository.findByStatusAndEndAtBefore(
            Exam.Status.PUBLISHED,
            now
        );
        
        logger.info("Found {} expired exams", expiredExams.size());

        for (Exam exam : expiredExams) {
            logger.info("Closing exam: {} (ID: {})", exam.getName(), exam.getId());
            // Đóng bài kiểm tra
            exam.setStatus(Exam.Status.CLOSED);
            examRepository.save(exam);

            // Tự động thêm câu hỏi public vào chương tương ứng
            questionService.autoAddPublicQuestionsToChapters(exam);
            
            // Tự động thêm bài kiểm tra vào danh sách public
            examService.autoAddExamToPublic(exam);
        }
    }
} 