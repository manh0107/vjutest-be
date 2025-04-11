package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.AnswerDTO;
import com.example.vjutest.Model.Answer;

@Component
public class AnswerMapper {
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;

    @Autowired
    public AnswerMapper(UserMapper userMapper, QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
        this.userMapper = userMapper;
    }

    public AnswerDTO toSimpleDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setAnswerName(answer.getAnswerName());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setModifiedAt(answer.getModifiedAt());
        
        if (answer.getQuestion() != null) {
            dto.setQuestionId(answer.getQuestion().getId());
            dto.setQuestionName(answer.getQuestion().getName());
        }

        if (answer.getCreatedBy() != null) {
            dto.setCreatedById(answer.getCreatedBy().getId());
            dto.setCreatedByName(answer.getCreatedBy().getName());
        }
        
        return dto;
    }

    public AnswerDTO toFullDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setAnswerName(answer.getAnswerName());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setModifiedAt(answer.getModifiedAt());

        if (answer.getCreatedBy() != null) {
            dto.setUser(userMapper.toDTO(answer.getCreatedBy()));
        }

        if (answer.getQuestion() != null) {
            dto.setQuestion(questionMapper.toFullDTO(answer.getQuestion()));
        }

        return dto;
    }
}
