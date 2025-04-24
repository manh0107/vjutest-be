package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.MajorDTO;
import com.example.vjutest.Model.Major;


@Component
public class MajorMapper {
    private final SubjectMapper subjectMapper;
    private final DepartmentMapper departmentMapper;

    @Autowired
    public MajorMapper(SubjectMapper subjectMapper, DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
        this.subjectMapper = subjectMapper;
    }
    public MajorDTO toDTO(Major major) {
        if (major == null) {
            return null;
        }

        MajorDTO dto = new MajorDTO();
        dto.setId(major.getId());
        dto.setName(major.getName());
        dto.setCreatedAt(major.getCreatedAt());
        dto.setModifiedAt(major.getModifiedAt());

        if (major.getCreatedBy() != null) {
            dto.setCreatedById(major.getCreatedBy().getId());
            dto.setCreatedByName(major.getCreatedBy().getName());
        }

        if (major.getModifiedBy() != null) {
            dto.setModifiedById(major.getModifiedBy().getId());
            dto.setModifiedByName(major.getModifiedBy().getName());
        }

        if (major.getSubjects() != null) {
            dto.setSubjects(major.getSubjects().stream()
                .map(subject -> subjectMapper.toDTO(subject))
                .toList());
        }

        if (major.getDepartment() != null) {
            dto.setDepartment(departmentMapper.toDTO(major.getDepartment()));
        }

        dto.setDepartmentId(major.getDepartment() != null ? major.getDepartment().getId() : null);

        return dto;
    }    
}
