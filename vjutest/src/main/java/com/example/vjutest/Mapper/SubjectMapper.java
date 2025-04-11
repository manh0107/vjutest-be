package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Model.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {
    public SubjectDTO toDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setSubjectCode(subject.getSubjectCode());
        dto.setDescription(subject.getDescription());
        dto.setCreditHour(subject.getCreditHour());
        dto.setCreatedById(subject.getCreatedBy().getId()); 
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setCreatedByName(subject.getCreatedBy().getName());
        return dto;
    }
}
