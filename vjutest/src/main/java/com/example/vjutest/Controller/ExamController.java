package com.example.vjutest.Controller;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Request.UpdateExamStatusRequest;
import com.example.vjutest.Service.ExamService;

import org.springframework.http.HttpStatus;
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

    //Tạo bài kiểm tra
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
    @GetMapping("/find/{examId}")
    public ResponseEntity<ExamDTO> getExamById(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        Optional<ExamDTO> exam = examService.getExamById(examId, userId);
        return exam.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Cập nhật status để  hoàn thành tạo bài thi
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
    @PostMapping("/create-without-class")
    public ResponseEntity<ExamDTO> createExamWithoutClass(
            @RequestParam Long subjectId,
            @RequestParam Long userId,
            @RequestBody Exam examRequest) {
        
        ExamDTO createdExam = examService.createExamWithoutClass(subjectId, userId, examRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
    }
    
    //Lấy danh sách bài kiểm tra public theo môn học
    @GetMapping("/public-exams/{subjectId}")
    public ResponseEntity<List<ExamDTO>> getExamsBySubjectAndUser(@RequestParam Long subjectId, @RequestParam Long userId) {
        return ResponseEntity.ok(examService.getExamsBySubjectAndUser(subjectId, userId));
    }
}
