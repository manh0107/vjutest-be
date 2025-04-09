package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Service.QuestionService;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

    @Autowired
    public QuestionController(QuestionService questionService, QuestionMapper questionMapper) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestBody Question questionRequest,
            @RequestParam Long userId,
            @RequestParam Long subjectId) {      
        QuestionDTO question = questionService.createQuestion(questionRequest, userId, subjectId);
        return ResponseEntity.ok(question);
    }

    // Tạo câu hỏi bên trong bài kiểm tra (có thể public hoặc private)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create-in-exam")
    public ResponseEntity<QuestionDTO> createQuestionInExam(
            @RequestBody Question questionRequest,
            @RequestParam Long examId,
            @RequestParam Long userId,
            @RequestParam Long subjectId) {
        QuestionDTO question = questionService.createQuestionInExam(questionRequest, examId, userId, subjectId);
        return ResponseEntity.ok(question);
    }

    // Lấy danh sách câu hỏi từ ngân hàng câu hỏi
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @GetMapping("/bank")
    public ResponseEntity<List<QuestionDTO>> getQuestionsFromBank() {
        List<Question> questions = questionService.getQuestionsFromBank();
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(questionMapper::toSimpleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }

    // Lấy danh sách câu hỏi công khai theo exam
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @GetMapping("/bank/{examId}")
    public ResponseEntity<List<QuestionDTO>> getPublicQuestionsByExam(@PathVariable Long examId) {
        List<Question> questions = questionService.getPublicQuestionsByExam(examId);
        List<QuestionDTO> dtos = questions.stream()
            .map(questionMapper::toSimpleDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Gán các câu hỏi từ ngân hàng vào bài kiểm tra
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/bank/assign")
    public ResponseEntity<String> assignQuestionsToExam(
            @RequestParam Long examId,
            @RequestParam Long userId,
            @RequestBody List<Long> questionIds) {

        questionService.addQuestionsToExam(examId, userId, questionIds);
        return ResponseEntity.ok("Gán câu hỏi vào bài kiểm tra thành công!");
    }
}
