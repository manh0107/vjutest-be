package com.example.vjutest.Mapper;

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

        // Lấy thông tin người tạo
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
            dto.setExamQuestions(examQuestionDTO);
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

        // Lấy thông tin người tạo
        if (question.getCreatedBy() != null) {
            dto.setCreatedBy(question.getCreatedBy().getId());
            dto.setCreatedByName(question.getCreatedBy().getName());
        }

        // Lấy thông tin lớp học - môn học chi tiết
        if (question.getExamQuestions() != null) {
            dto.setExamQuestions(question.getExamQuestions().stream()
                .map(examQuestionMapper::toFullDTO)
                .findFirst()
                .orElse(null));
        }

        if (question.getSubject() != null) {
            dto.setSubject(subjectMapper.toDTO(question.getSubject()));
        }

        return dto;
    }
}
