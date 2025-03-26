package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.Model.Exam;

@Component
public class ExamMapper {

    private final ClassSubjectMapper classSubjectMapper;

    @Autowired
    public ExamMapper(ClassSubjectMapper classSubjectMapper) {
        this.classSubjectMapper = classSubjectMapper;
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
        dto.setPublic(exam.isPublic());
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
        dto.setPublic(exam.isPublic());
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

        return dto;
    }
}
