package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ChapterDTO;
import com.example.vjutest.Model.Chapter;

@Component
public class ChapterMapper {
    private final SubjectMapper subjectMapper;

    @Autowired
    public ChapterMapper(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    public ChapterDTO toDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setName(chapter.getName());
        dto.setCreatedAt(chapter.getCreatedAt());
        dto.setModifiedAt(chapter.getModifiedAt());
        dto.setQuestionTotal(chapter.getQuestionTotal());

        if (chapter.getCreatedBy() != null) {
            dto.setCreatedById(chapter.getCreatedBy().getId());
            dto.setCreatedByName(chapter.getCreatedBy().getName());
        }

        if(chapter.getModifiedBy() != null) {
            dto.setModifiedById(chapter.getModifiedBy().getId());
            dto.setModifiedByName(chapter.getModifiedBy().getName());
        }

        if(chapter.getSubject() != null) {
            dto.setSubject(subjectMapper.toDTO(chapter.getSubject()));
        }

        return dto;
    }
}
