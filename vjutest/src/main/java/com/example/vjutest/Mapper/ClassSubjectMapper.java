package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ClassSubjectDTO;
import com.example.vjutest.Model.ClassSubject;

@Component
public class ClassSubjectMapper {

    private final ClassEntityMapper classEntityMapper;
    private final SubjectMapper subjectMapper;

    @Autowired
    public ClassSubjectMapper(ClassEntityMapper classEntityMapper, SubjectMapper subjectMapper) {
        this.classEntityMapper = classEntityMapper;
        this.subjectMapper = subjectMapper;
    }

    public ClassSubjectDTO toFullDTO(ClassSubject classSubject) {
        if (classSubject == null) {
            return null;
        }

        ClassSubjectDTO dto = new ClassSubjectDTO();
        dto.setId(classSubject.getId());

        // Chuyển đổi ClassEntity sang DTO
        dto.setClassEntity(classSubject.getClassEntity() != null ? classEntityMapper.toFullDTO(classSubject.getClassEntity()) : null);

        // Chuyển đổi Subject sang DTO
        dto.setSubject(classSubject.getSubject() != null ? subjectMapper.toDTO(classSubject.getSubject()) : null);

        // Lấy đường dẫn tài liệu
        dto.setDocumentUrl(classSubject.getDocumentUrl());

        dto.setFileName(classSubject.getFileName());

        return dto;
    }

    public ClassSubjectDTO toSimpleDTO(ClassSubject classSubject) {
        if (classSubject == null) {
            return null;
        }

        ClassSubjectDTO dto = new ClassSubjectDTO();
        dto.setId(classSubject.getId());

        // Chuyển đổi ClassEntity sang DTO đơn giản
        dto.setClassEntity(classSubject.getClassEntity() != null ? classEntityMapper.toDTO(classSubject.getClassEntity()) : null);

        // Chuyển đổi Subject sang DTO đơn giản
        dto.setSubject(classSubject.getSubject() != null ? subjectMapper.toDTO(classSubject.getSubject()) : null);

        // Lấy đường dẫn tài liệu
        dto.setDocumentUrl(classSubject.getDocumentUrl());

        dto.setFileName(classSubject.getFileName());

        return dto;
    }
}
