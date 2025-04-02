package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Exam.Status;

@Component
public class ExamMapper {

    private final ClassSubjectMapper classSubjectMapper;
    private final SubjectMapper subjectMapper;

    @Autowired
    public ExamMapper(ClassSubjectMapper classSubjectMapper, SubjectMapper subjectMapper) {
        this.classSubjectMapper = classSubjectMapper;
        this.subjectMapper = subjectMapper;
    }

    // Chuyển đổi Exam -> ExamDTO (Đơn giản)
    public ExamDTO toSimpleDTO(Exam exam) {
        if (exam == null) {
            return null;
        }

        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setExamCode(exam.getExamCode());
        dto.setDescription(exam.getDescription());
        dto.setDurationTime(exam.getDurationTime());
        dto.setMaxScore(exam.getMaxScore());
        dto.setStatus(exam.getStatus());
        dto.setIsPublic(exam.getIsPublic());
        dto.setStartAt(exam.getStartAt());
        dto.setEndAt(exam.getEndAt());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setModifiedAt(exam.getModifiedAt());

        // Lấy thông tin người tạo
        if (exam.getCreatedBy() != null) {
            dto.setCreatedBy(exam.getCreatedBy().getId());
            dto.setCreatedByName(exam.getCreatedBy().getName());
        }

        // Lấy thông tin lớp học - môn học
        if (exam.getClassSubject() != null) {
            if (exam.getClassSubject().getClassEntity() != null) {
                dto.setClassId(exam.getClassSubject().getClassEntity().getId());
                dto.setClassName(exam.getClassSubject().getClassEntity().getName());
            }
            if (exam.getClassSubject().getSubject() != null) {
                dto.setSubjectId(exam.getClassSubject().getSubject().getId());
                dto.setSubjectName(exam.getClassSubject().getSubject().getName());
            }
        }

        if (exam.getSubject() != null) {
            SubjectDTO subjectDTO = new SubjectDTO();
            subjectDTO.setId(exam.getSubject().getId());
            subjectDTO.setName(exam.getSubject().getName());
            dto.setSubject(subjectDTO);
        }
        
        return dto;
    }

    // Chuyển đổi Exam -> ExamDTO (Chi tiết)
    public ExamDTO toFullDTO(Exam exam) {
        if (exam == null) {
            return null;
        }

        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setExamCode(exam.getExamCode());
        dto.setDescription(exam.getDescription());
        dto.setDurationTime(exam.getDurationTime());
        dto.setMaxScore(exam.getMaxScore());
        dto.setStatus(exam.getStatus());
        dto.setIsPublic(exam.getIsPublic());
        dto.setStartAt(exam.getStartAt());
        dto.setEndAt(exam.getEndAt());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setModifiedAt(exam.getModifiedAt());

        // Lấy thông tin người tạo
        if (exam.getCreatedBy() != null) {
            dto.setCreatedBy(exam.getCreatedBy().getId());
            dto.setCreatedByName(exam.getCreatedBy().getName());
        }

        // Lấy thông tin lớp học - môn học chi tiết
        if (exam.getClassSubject() != null) {
            dto.setClassSubject(classSubjectMapper.toFullDTO(exam.getClassSubject()));
        }

        if (exam.getSubject() != null) {
            dto.setSubject(subjectMapper.toDTO(exam.getSubject()));
        }

        return dto;
    }

    public Exam toEntity(ExamDTO dto) {
        if (dto == null) {
            return null;
        }
    
        Exam exam = new Exam();
        exam.setId(dto.getId());

        if (dto.getStatus() != null) {
            try {
                exam.setStatus(Status.valueOf(dto.getStatus().name())); 
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
            }
        } else {
            exam.setStatus(Status.DRAFT); // Giá trị mặc định nếu `status` là null
        }
    
        return exam;
    }
    
}
