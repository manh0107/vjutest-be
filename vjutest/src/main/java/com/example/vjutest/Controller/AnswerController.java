package com.example.vjutest.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.DTO.AnswerDTO;
import com.example.vjutest.Service.AnswerService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.Service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<List<AnswerDTO>> createAnswer(
        @RequestParam Long questionId,
        Authentication authentication,
        @RequestPart("answers") List<AnswerDTO> answerRequest,
        @RequestPart(required = false) List<MultipartFile> imageFiles) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<AnswerDTO> answer = answerService.createAnswersForQuestion(questionId, userId, answerRequest, imageFiles);
        return ResponseEntity.ok(answer);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<AnswerDTO> updateAnswer(
        @PathVariable Long id, 
        @RequestPart("answer") AnswerDTO answerRequest, 
        Authentication authentication, 
        @RequestParam Long questionId,
        @RequestPart(required = false) MultipartFile imageFile) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(answerService.updateAnswer(id, answerRequest, userId, questionId, imageFile));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        answerService.deleteAnswer(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-question/{questionId}")
    public ResponseEntity<List<AnswerDTO>> getAnswersByQuestion(@PathVariable Long questionId) {
        List<AnswerDTO> answers = answerService.getAnswersByQuestionDTO(questionId);
        return ResponseEntity.ok(answers);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create-single")
    public ResponseEntity<AnswerDTO> createSingleAnswer(
        @RequestParam Long questionId,
        Authentication authentication,
        @RequestPart("answer") AnswerDTO answerRequest,
        @RequestPart(required = false) MultipartFile imageFile) throws Exception {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(answerService.createSingleAnswer(questionId, userId, answerRequest, imageFile));
    }
}
