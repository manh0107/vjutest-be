package com.example.vjutest.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.DTO.ExamQuestionDTO; // Added import
import com.example.vjutest.DTO.QuestionDTO; // Added import
import com.example.vjutest.Model.Question;


@Component
public class QuestionMapper {

    private final SubjectMapper subjectMapper;
    private final ExamQuestionMapper examQuestionMapper;

    @Autowired
    public QuestionMapper( SubjectMapper subjectMapper, ExamQuestionMapper examQuestionMapper) {
        this.examQuestionMapper = examQuestionMapper;
        this.subjectMapper = subjectMapper;
    }

    //Simple DTO
    public QuestionDTO toSimpleDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setName(question.getName());
        dto.setDifficulty(question.getDifficulty());
        dto.setIsPublic(question.getIsPublic());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setModifiedAt(question.getModifiedAt());

        if(question.getAnswers() != null) {
            
        }

        if (question.getCreatedBy() != null) {
            dto.setCreatedBy(question.getCreatedBy().getId());
            dto.setCreatedByName(question.getCreatedBy().getName());
        }

        if (question.getSubject() != null) {
            SubjectDTO subjectDTO = new SubjectDTO();
            subjectDTO.setId(question.getSubject().getId());
            subjectDTO.setName(question.getSubject().getName());
            dto.setSubject(subjectDTO);
        }

        if (question.getExamQuestions() != null) {
            ExamQuestionDTO examQuestionDTO = new ExamQuestionDTO();
            question.getExamQuestions().forEach(examQuestion -> {
                if (examQuestion.getExam() != null) {
                    examQuestionDTO.setId(examQuestion.getExam().getId());
                    examQuestionDTO.setExamName(examQuestion.getExam().getName());
                }
                if (examQuestion.getQuestion() != null) {
                    examQuestionDTO.setId(examQuestion.getQuestion().getId());
                    examQuestionDTO.setSubjectName(examQuestion.getQuestion().getName());
                }
            });
            dto.setExamQuestions(Collections.singletonList(examQuestionDTO));
        }
        
        return dto;
    }

    //Full DTO
    public QuestionDTO toFullDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setName(question.getName());
        dto.setDifficulty(question.getDifficulty());
        dto.setIsPublic(question.getIsPublic());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setModifiedAt(question.getModifiedAt());

        if (question.getCreatedBy() != null) {
            dto.setCreatedBy(question.getCreatedBy().getId());
            dto.setCreatedByName(question.getCreatedBy().getName());
        }

        if (question.getExamQuestions() != null) {
            List<ExamQuestionDTO> examQuestionDTOs = question.getExamQuestions().stream()
                .map(examQuestionMapper::toFullDTO)
                .collect(Collectors.toList());
            dto.setExamQuestions(examQuestionDTOs);
        }

        if (question.getSubject() != null) {
            dto.setSubject(subjectMapper.toDTO(question.getSubject()));
        }

        return dto;
    }
}
