package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Model.Subject.VisibilityScope;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubjectMapper {

    @Autowired
    private UserRepository userRepository;


    public SubjectDTO toDTO(Subject subject) {
        if (subject == null) return null;
        
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setSubjectCode(subject.getSubjectCode());
        dto.setDescription(subject.getDescription());
        dto.setCreditHour(subject.getCreditHour());
        dto.setVisibility(subject.getVisibility());

        if (subject.getCreatedBy() != null) {
            dto.setCreatedById(subject.getCreatedBy().getId());
            dto.setCreatedByName(subject.getCreatedBy().getName());
        }

        if (subject.getModifiedBy() != null) {
            dto.setModifiedById(subject.getModifiedBy().getId());
            dto.setModifiedByName(subject.getModifiedBy().getName());
        }

        dto.setCreatedAt(subject.getCreatedAt());
        dto.setModifiedAt(subject.getModifiedAt());

        // Map majors to list of ids
        if (subject.getMajors() != null && !subject.getMajors().isEmpty()) {
            dto.setMajorIds(subject.getMajors().stream().map(m -> m.getId()).toList());
        }

        // Map departments to list of ids
        if (subject.getDepartments() != null && !subject.getDepartments().isEmpty()) {
            dto.setDepartmentIds(subject.getDepartments().stream().map(d -> d.getId()).toList());
        }

        return dto;
    }

    public Subject toEntity(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setId(dto.getId());
        subject.setName(dto.getName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setDescription(dto.getDescription());
        subject.setCreditHour(dto.getCreditHour());

        if (dto.getCreatedById() != null) {
            Optional<User> createdBy = userRepository.findById(dto.getCreatedById());
            createdBy.ifPresent(subject::setCreatedBy);
        }

        if (dto.getModifiedById() != null) {
            Optional<User> modifiedBy = userRepository.findById(dto.getModifiedById());
            modifiedBy.ifPresent(subject::setModifiedBy);
        }

        if (dto.getVisibility() != null) {
            try {
                subject.setVisibility(VisibilityScope.valueOf(dto.getVisibility().name())); 
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getVisibility());
            }
        } else {
            subject.setVisibility(VisibilityScope.PUBLIC);

        }
        return subject;
    }
}