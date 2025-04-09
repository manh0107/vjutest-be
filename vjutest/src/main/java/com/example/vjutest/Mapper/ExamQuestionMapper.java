package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ExamQuestionDTO;
import com.example.vjutest.Model.ExamQuestion;

@Component
public class ExamQuestionMapper {
    
    private final ExamMapper examMapper;

    @Autowired
    public ExamQuestionMapper(ExamMapper examMapper) {
        this.examMapper = examMapper;
    }
    
    public ExamQuestionDTO toFullDTO(ExamQuestion examQuestion) {
        if (examQuestion == null) {
            return null;
        }

        ExamQuestionDTO dto = new ExamQuestionDTO();
        dto.setId(examQuestion.getId());
        dto.setPoint(examQuestion.getPoint());

        dto.setExam(examQuestion.getExam() != null ? examMapper.toFullDTO(examQuestion.getExam()) : null);

        return dto;
    }

    public ExamQuestionDTO toSimpleDTO(ExamQuestion examQuestion) {
        if (examQuestion == null) {
            return null;
        }

        ExamQuestionDTO dto = new ExamQuestionDTO();
        dto.setId(examQuestion.getId());
        dto.setPoint(examQuestion.getPoint());

        dto.setExam(examQuestion.getExam() != null ? examMapper.toSimpleDTO(examQuestion.getExam()) : null);
        return dto;
    }
}
