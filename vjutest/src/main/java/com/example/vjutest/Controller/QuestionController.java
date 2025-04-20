package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Service.QuestionService;
import com.example.vjutest.User.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;


    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestBody Question questionRequest,
            @RequestParam Long subjectId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        QuestionDTO question = questionService.createQuestion(questionRequest, userId, subjectId);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/create-in-exam")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestionInExam(
            @RequestBody Question questionRequest,
            @RequestParam Long examId,
            @RequestParam Long subjectId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        QuestionDTO question = questionService.createQuestionInExam(questionRequest, examId, userId, subjectId);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/bank")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsFromBank() {
        List<Question> questions = questionService.getQuestionsFromBank();
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(questionMapper::toSimpleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }

    @GetMapping("/bank/{examId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<QuestionDTO>> getPublicQuestionsByExam(@PathVariable Long examId) {
        List<Question> questions = questionService.getPublicQuestionsByExam(examId);
        List<QuestionDTO> dtos = questions.stream()
            .map(questionMapper::toSimpleDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/bank/assign")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<String> assignQuestionsToExam(
            @RequestParam Long examId,
            @RequestBody List<Long> questionIds,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        questionService.addQuestionsToExam(examId, userId, questionIds);
        return ResponseEntity.ok("Gán câu hỏi vào bài kiểm tra thành công!");
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> updateBankQuestion(
            @PathVariable Long id,
            @RequestBody QuestionDTO questionRequest,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.updateBankQuestion(id, questionRequest, userId));
    }

    @PutMapping("/update-in-exam/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> updateQuestionInExam(
            @PathVariable Long id,
            @RequestParam Long examId,
            @RequestBody QuestionDTO questionRequest,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.updateQuestionInExam(id, questionRequest, examId, userId));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        questionService.deleteQuestion(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByExam(@PathVariable Long examId, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId, userId));
    }
}
