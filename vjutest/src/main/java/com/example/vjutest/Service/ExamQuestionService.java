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
        ExamQuestion examQuestion = new ExamQuestion();
        
        if (Boolean.FALSE.equals(question.getIsPublic())) {
            if (point == null || point <= 0) {
                throw new RuntimeException("Câu hỏi riêng phải có điểm lớn hơn 0!");
            }
            examQuestion.setPoint(point);
        } else {
            examQuestion.setPoint(1); // mặc định cho public
        }
        
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        return examQuestionRepository.save(examQuestion);
    }
}
