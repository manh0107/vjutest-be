package com.example.vjutest.Controller;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.DTO.UserAnswerDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Request.UpdateExamStatusRequest;
import com.example.vjutest.Service.ExamService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.DTO.ExamStatisticsDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    // Tạo bài kiểm tra
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<ExamDTO> createExam(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestBody Exam examRequest,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ExamDTO exam = examService.createExam(classId, subjectId, userId, examRequest);
        return ResponseEntity.ok(exam);
    }

    // Lấy danh sách bài kiểm tra trong lớp
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ExamDTO>> getExamsInClass(
            @PathVariable Long classId,
            @RequestParam Long subjectId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<ExamDTO> exams = examService.getExamsInClass(classId, subjectId, userId);
        return ResponseEntity.ok(exams);
    }

    // Lấy thông tin bài kiểm tra theo ID
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/find/{examId}")
    public ResponseEntity<ExamDTO> getExamById(
            @PathVariable Long examId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Optional<ExamDTO> exam = examService.getExamById(examId, userId);
        return exam.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cập nhật status để hoàn thành tạo bài thi
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @PutMapping("/{examId}/status")
    public ResponseEntity<ExamDTO> updateExamStatus(
            @PathVariable Long examId,
            @RequestBody UpdateExamStatusRequest request,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ExamDTO updatedExam = examService.updateExamStatus(
                examId,
                request.getNewStatus(),
                request.getStartAt(),
                request.getEndAt(),
                userId,
                request.getPassPercent(),
                request.getDurationTime()
        );
        return ResponseEntity.ok(updatedExam);
    }

    // Tạo bài kiểm tra bên ngoài lớp học
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @PostMapping("/create-without-class")
    public ResponseEntity<ExamDTO> createExamWithoutClass(
            @RequestParam Long subjectId,
            @RequestBody Exam examRequest,
            @RequestParam(required = false) List<Long> departmentIds,
            @RequestParam(required = false) List<Long> majorIds,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ExamDTO createdExam = examService.createExamWithoutClass(subjectId, userId, examRequest, departmentIds, majorIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
    }

    // Lấy danh sách bài kiểm tra public theo môn học
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/public-exams/{subjectId}")
    public ResponseEntity<List<ExamDTO>> getExamsBySubjectAndUser(
            @PathVariable Long subjectId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(examService.getExamsBySubjectAndUser(subjectId, userId));
    }

    // Sinh viên làm bài kiểm tra
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/start-exam/{examId}")
    public ResponseEntity<ResultDTO> startExam(
            @PathVariable Long examId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ResultDTO result = examService.startExam(examId, userId);
        return ResponseEntity.ok(result);
    }

    // Cho phép sinh viên làm lại bài kiểm tra
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @PutMapping("/allow-retake/{examId}")
    public ResponseEntity<ResultDTO> allowStudentToRetake(
            @PathVariable Long examId,
            @RequestParam Long studentId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ResultDTO result = examService.allowStudentToRetake(examId, studentId, userId);
        return ResponseEntity.ok(result);
    }

    // Sinh viên chọn đáp án
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/select-answer/{examId}")
    public ResponseEntity<UserAnswerDTO> chooseAnswer(
            @PathVariable Long examId,
            @RequestParam Long questionId,
            @RequestParam Long answerId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        UserAnswerDTO userAnswer = examService.chooseAnswer(examId, userId, questionId, answerId);
        return ResponseEntity.ok(userAnswer);
    }

    // Sinh viên nộp bài kiểm tra
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/submit-exam/{examId}")
    public ResponseEntity<ResultDTO> submitExam(
            @PathVariable Long examId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ResultDTO result = examService.submitExam(examId, userId);
        return ResponseEntity.ok(result);
    }

    // Lấy danh sách kết quả của sinh viên
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/results/{examId}")
    public ResponseEntity<List<ResultDTO>> getResults(
            @PathVariable Long examId,
            Authentication authentication) {

        Long studentId = Long.parseLong(authentication.getName());
        List<ResultDTO> results = examService.getResults(studentId, examId);
        return ResponseEntity.ok(results);
    }

    // Cập nhật bài kiểm tra (không thuộc lớp học)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ExamDTO> updateExamWithoutClass(
            @PathVariable Long id,
            @RequestBody Exam examRequest,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(examService.updateExamWithoutClass(id, userId, examRequest));
    }

    //Cập nhật bài kiểm tra bên trong lớp học
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @PutMapping("/update-in-class/{id}")
    public ResponseEntity<ExamDTO> updateExamInClass(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam Long classId,
            @RequestBody Exam examRequest) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(examService.updateExamInClass(id, userId, classId, examRequest));
    }

    // Xoá bài kiểm tra
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExam(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        examService.deleteExam(id, userId);
        return ResponseEntity.ok().build();
    }

    // Lấy thống kê bài kiểm tra
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @GetMapping("/statistics/{examId}")
    public ResponseEntity<ExamStatisticsDTO> getExamStatistics(
            @PathVariable Long examId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(examService.getExamStatistics(examId, userId));
    }

    // Xoá câu hỏi khỏi bài kiểm tra
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TEACHER')")
    @DeleteMapping("/delete-question/{questionId}")
    public ResponseEntity<Void> deleteQuestionFromExam(
            @PathVariable Long questionId,
            @RequestParam Long examId,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        examService.deleteQuestionFromExam(questionId, examId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{examId}/to-draft")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<ExamDTO> revertToDraft(@PathVariable Long examId, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ExamDTO exam = examService.revertToDraft(examId, userId);
        return ResponseEntity.ok(exam);
    }
}
