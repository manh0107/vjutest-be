package com.example.vjutest.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import com.example.vjutest.DTO.AnswerDTO;
import com.example.vjutest.Service.AnswerService;
import com.example.vjutest.User.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<List<AnswerDTO>> createAnswer(
        @RequestParam Long questionId,
        Authentication authentication,
        @RequestBody List<AnswerDTO> answerRequest ) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<AnswerDTO> answer = answerService.createAnswersForQuestion(questionId, userId, answerRequest);
        return ResponseEntity.ok(answer);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<AnswerDTO> updateAnswer(@PathVariable Long id, @RequestBody AnswerDTO answerRequest, Authentication authentication, @RequestParam Long questionId) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(answerService.updateAnswer(id, answerRequest, userId, questionId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        answerService.deleteAnswer(id, userId);
        return ResponseEntity.ok().build();
    }
}
