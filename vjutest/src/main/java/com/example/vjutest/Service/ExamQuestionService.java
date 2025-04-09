package com.example.vjutest.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.ExamQuestion;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Repository.ExamQuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    @Autowired
    private final ExamQuestionRepository examQuestionRepository;

    public ExamQuestion createExamQuestion(Exam exam, Question question, Integer point) {
        if (Boolean.FALSE.equals(question.getIsPublic()) && point == null) {
            throw new RuntimeException("Câu hỏi riêng phải có điểm!");
        }

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setPoint(Boolean.TRUE.equals(question.getIsPublic()) ? 1 : point);
        return examQuestionRepository.save(examQuestion);
    }
}
