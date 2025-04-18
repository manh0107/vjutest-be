package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.UserAnswerDTO;
import com.example.vjutest.Model.UserAnswer;

@Component
public class UserAnswerMapper {
    private final ResultMapper resultMapper;

    @Autowired
    public UserAnswerMapper(ResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    public UserAnswerDTO toDTO(UserAnswer userAnswer) {
        if (userAnswer == null) {
            return null;
        }

        UserAnswerDTO dto = new UserAnswerDTO();
        dto.setId(userAnswer.getId());
        dto.setUserId(userAnswer.getUser().getId());
        dto.setQuestionId(userAnswer.getQuestion().getId());
        dto.setAnswerId(userAnswer.getAnswer().getId());
        dto.setExamId(userAnswer.getExam().getId());
        dto.setIsSubmitted(userAnswer.getIsSubmitted());

        if(userAnswer.getResult() != null) {
            dto.setResult(resultMapper.toFullDTO(userAnswer.getResult()));
        }

        return dto;
    }
}
