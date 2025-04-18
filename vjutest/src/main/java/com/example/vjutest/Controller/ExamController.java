package com.example.vjutest.Controller;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.DTO.UserAnswerDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Request.UpdateExamStatusRequest;
import com.example.vjutest.Service.ExamService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    //Tạo bài kiểm tra
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<ExamDTO> createExam(
            @RequestParam Long classId,                                                            
            @RequestParam Long subjectId,
            @RequestParam Long userId,
            @RequestBody Exam examRequest) {
        ExamDTO exam = examService.createExam(classId, subjectId, userId, examRequest);
        return ResponseEntity.ok(exam);
    }

    //Lấy danh sách bài kiểm tra trong lớp
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ExamDTO>> getExamsInClass(
            @PathVariable Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long userId
            ) {
        List<ExamDTO> exams = examService.getExamsInClass(classId, subjectId, userId); 
        return ResponseEntity.ok(exams);
    }

    //Lấy thông tin bài kiểm tra theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/find/{examId}")
    public ResponseEntity<ExamDTO> getExamById(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        Optional<ExamDTO> exam = examService.getExamById(examId, userId);
        return exam.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Cập nhật status để  hoàn thành tạo bài thi
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PutMapping("/{examId}/status")
    public ResponseEntity<ExamDTO> updateExamStatus(
            @PathVariable Long examId,
            @RequestParam Long userId,
            @RequestBody UpdateExamStatusRequest request
            
    ) {
        ExamDTO updatedExam = examService.updateExamStatus(
                examId,
                request.getNewStatus(),
                request.getStartAt(),
                request.getEndAt(),
                userId
        );
        return ResponseEntity.ok(updatedExam);
    }

    //Tạo bài kiểm tra bên ngoài lớp học
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create-without-class")
    public ResponseEntity<ExamDTO> createExamWithoutClass(
            @RequestParam Long subjectId,
            @RequestParam Long userId,
            @RequestBody Exam examRequest) {
        
        ExamDTO createdExam = examService.createExamWithoutClass(subjectId, userId, examRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
    }
    
    //Lấy danh sách bài kiểm tra public theo môn học
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/public-exams/{subjectId}")
    public ResponseEntity<List<ExamDTO>> getExamsBySubjectAndUser(@RequestParam Long subjectId, @RequestParam Long userId) {
        return ResponseEntity.ok(examService.getExamsBySubjectAndUser(subjectId, userId));
    }

    //Sinh viên làm bài kiểm tra
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/start-exam/{examId}")
    public ResponseEntity<ResultDTO> startExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        ResultDTO result = examService.startExam(examId, userId);
        return ResponseEntity.ok(result);
    }

    //Cho phép sinh viên làm lại bài kiểm tra
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PutMapping("/allow-retake/{examId}")
    public ResponseEntity<ResultDTO> allowStudentToRetake(
            @PathVariable Long examId,
            @RequestParam Long studentId,
            @RequestParam Long userId) {
        ResultDTO result = examService.allowStudentToRetake(examId, studentId, userId);
        return ResponseEntity.ok(result);
    }

    //Sinh viên chọn đáp án
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/select-answer/{examId}")
    public ResponseEntity<UserAnswerDTO> chooseAnswer(
            @PathVariable Long examId,
            @RequestParam Long userId,
            @RequestParam Long questionId,
            @RequestParam Long answerId) {
        UserAnswerDTO userAnswer = examService.chooseAnswer(examId, userId, questionId, answerId);
        return ResponseEntity.ok(userAnswer);
    }

    //Sinh viên nộp bài kiểm tra
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/submit-exam/{examId}")
    public ResponseEntity<ResultDTO> submitExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        ResultDTO result = examService.submitExam(examId, userId);
        return ResponseEntity.ok(result);
    }

    //Lấy danh sách kết quả cuả sinh viên
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/results/{examId}")
    public ResponseEntity<List<ResultDTO>> getResults(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        List<ResultDTO> results = examService.getResults(examId, userId);
        return ResponseEntity.ok(results);
    }
}
