package com.example.vjutest.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Service.QuestionService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.Service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestPart("question") Question questionRequest,
            @RequestParam Long chapterId,
            @RequestPart(required = false) MultipartFile imageFile,
            Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        QuestionDTO question = questionService.createQuestion(questionRequest, userId, chapterId, imageFile);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/create-in-exam")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestionInExam(
            @RequestPart("question") Question questionRequest,
            @RequestParam Long examId,
            @RequestParam Long chapterId,
            @RequestPart(required = false) MultipartFile imageFile,
            Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        QuestionDTO question = questionService.createQuestionInExam(questionRequest, examId, userId, chapterId, imageFile);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/bank/{examId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<QuestionDTO>> getPublicQuestionsByExam(
            @PathVariable Long examId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<Question> questions = questionService.getPublicQuestionsByExam(examId, userId);
        List<QuestionDTO> dtos = questions.stream()
            .map(questionMapper::toFullDTO)
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
            @RequestPart("question") QuestionDTO questionRequest,
            @RequestPart(required = false) MultipartFile imageFile,
            Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.updateBankQuestion(id, questionRequest, userId, imageFile));
    }

    @PutMapping("/update-in-exam/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<QuestionDTO> updateQuestionInExam(
            @PathVariable Long id,
            @RequestParam Long examId,
            @RequestPart("question") QuestionDTO questionRequest,
            @RequestPart(required = false) MultipartFile imageFile,
            Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.updateQuestionInExam(id, questionRequest, userId, examId, imageFile));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        questionService.deleteQuestion(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByExam(
            @PathVariable Long examId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId, userId));
    }

    @GetMapping("/by-chapter/{chapterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByChapter(@PathVariable Long chapterId) {
        List<Question> questions = questionService.getQuestionsByChapter(chapterId);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(questionMapper::toFullDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }

    @GetMapping("/completed/by-chapter/{chapterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<QuestionDTO>> getCompletedQuestionsByChapter(@PathVariable Long chapterId) {
        List<Question> questions = questionService.getCompletedQuestionsByChapter(chapterId);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(questionMapper::toFullDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }
}
