package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@Component
public class SubjectMapper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MajorRepository majorRepository;

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

        // Set the first major's ID if available
        if (subject.getMajors() != null && !subject.getMajors().isEmpty()) {
            Major firstMajor = subject.getMajors().iterator().next();
            dto.setMajorId(firstMajor.getId());
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
        subject.setVisibility(dto.getVisibility());

        if (dto.getCreatedById() != null) {
            Optional<User> createdBy = userRepository.findById(dto.getCreatedById());
            createdBy.ifPresent(subject::setCreatedBy);
        }

        if (dto.getModifiedById() != null) {
            Optional<User> modifiedBy = userRepository.findById(dto.getModifiedById());
            modifiedBy.ifPresent(subject::setModifiedBy);
        }

        // Handle major relationship
        if (dto.getMajorId() != null) {
            Optional<Major> major = majorRepository.findById(dto.getMajorId());
            if (major.isPresent()) {
                Set<Major> majors = new HashSet<>();
                majors.add(major.get());
                subject.setMajors(majors);
            }
        }

        return subject;
    }
}
