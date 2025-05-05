package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Model.Question;

@Component
public class QuestionMapper {
    private final ChapterMapper chapterMapper;
    private final ExamQuestionMapper examQuestionMapper;

    @Autowired
    public QuestionMapper(ChapterMapper chapterMapper, ExamQuestionMapper examQuestionMapper) {
        this.chapterMapper = chapterMapper;
        this.examQuestionMapper = examQuestionMapper;
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
        dto.setIsCompleted(question.getIsCompleted());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setModifiedAt(question.getModifiedAt());
        dto.setImageUrl(question.getImageUrl());
        dto.setMarkedAsPublic(question.getMarkedAsPublic());

        if (question.getCreatedBy() != null) {
            dto.setCreatedById(question.getCreatedBy().getId());
            dto.setCreatedByName(question.getCreatedBy().getName());
        }

        if (question.getChapter() != null) {
            dto.setChapterId(question.getChapter().getId());
            dto.setChapterName(question.getChapter().getName());
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
        dto.setIsCompleted(question.getIsCompleted());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setModifiedAt(question.getModifiedAt());
        dto.setImageUrl(question.getImageUrl());
        dto.setMarkedAsPublic(question.getMarkedAsPublic());
        if (question.getCreatedBy() != null) {
            dto.setCreatedById(question.getCreatedBy().getId());
            dto.setCreatedByName(question.getCreatedBy().getName());
        }

        if (question.getChapter() !=null) {
            dto.setChapter(chapterMapper.toDTO(question.getChapter()));
        }

        if (question.getExamQuestions() != null) {
            dto.setExamQuestions(
                question.getExamQuestions().stream()
                    .map(examQuestionMapper::toFullDTO)
                    .collect(java.util.stream.Collectors.toList())
            );
        }

        return dto;
    }
}
