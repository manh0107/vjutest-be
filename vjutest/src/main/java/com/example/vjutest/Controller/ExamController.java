package com.example.vjutest.Controller;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Service.ExamService;
import org.springframework.http.ResponseEntity;
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

    // API tạo bài kiểm tra
    @PostMapping("/create")
    public ResponseEntity<ExamDTO> createExam(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long userId,
            @RequestBody Exam examRequest) {
        ExamDTO exam = examService.createExam(classId, subjectId, userId, examRequest);
        return ResponseEntity.ok(exam);
    }

    // API lấy danh sách bài kiểm tra trong lớp
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ExamDTO>> getExamsInClass(
            @PathVariable Long classId,
            @RequestParam Long userId) {
        List<ExamDTO> exams = examService.getExamsInClass(classId, userId);
        return ResponseEntity.ok(exams);
    }

    // API lấy thông tin bài kiểm tra theo ID
    @GetMapping("/find/{examId}")
    public ResponseEntity<ExamDTO> getExamById(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        Optional<ExamDTO> exam = examService.getExamById(examId, userId);
        return exam.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
