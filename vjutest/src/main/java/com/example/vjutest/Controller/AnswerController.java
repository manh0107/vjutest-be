package com.example.vjutest.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vjutest.DTO.AnswerDTO;
import com.example.vjutest.Service.AnswerService;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    private final AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/create")
    public ResponseEntity<List<AnswerDTO>> createAnswer(
        @RequestParam Long questionId,
        @RequestParam Long userId,
        @RequestBody List<AnswerDTO> answerRequest ) {
        List<AnswerDTO> answer = answerService.createAnswersForQuestion(questionId, userId, answerRequest);
        return ResponseEntity.ok(answer);
    }
}
