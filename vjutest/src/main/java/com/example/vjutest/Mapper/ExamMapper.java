package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Exam.ExamVisibility;
import com.example.vjutest.Model.Exam.Status;
import com.example.vjutest.Repository.ChapterRepository;
import com.example.vjutest.Model.Chapter;
import java.util.HashSet;

@Component
public class ExamMapper {

    private final ClassSubjectMapper classSubjectMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;

    @Autowired
    public ExamMapper(ClassSubjectMapper classSubjectMapper, SubjectMapper subjectMapper, UserMapper userMapper, ChapterRepository chapterRepository, ChapterMapper chapterMapper) {
        this.classSubjectMapper = classSubjectMapper;
        this.subjectMapper = subjectMapper;
        this.userMapper = userMapper;
        this.chapterRepository = chapterRepository;
        this.chapterMapper = chapterMapper;
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
        dto.setPassScore(exam.getPassScore());
        dto.setMaxScore(exam.getMaxScore());
        dto.setIsPublic(exam.getIsPublic());
        dto.setStatus(exam.getStatus());
        dto.setVisibility(exam.getVisibility());
        dto.setQuestionsCount(exam.getQuestionsCount() != null ? exam.getQuestionsCount() : exam.getTotalQuestions());
        dto.setStartAt(exam.getStartAt());
        dto.setEndAt(exam.getEndAt());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setModifiedAt(exam.getModifiedAt());

        // Lấy thông tin người tạo
        if (exam.getCreatedBy() != null) {
            dto.setCreatedById(exam.getCreatedBy().getId());
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

        // Map majors to list of ids
        if (exam.getMajors() != null && !exam.getMajors().isEmpty()) {
            dto.setMajorIds(exam.getMajors().stream().map(m -> m.getId()).toList());
        }

        // Map departments to list of ids
        if (exam.getDepartments() != null && !exam.getDepartments().isEmpty()) {
            dto.setDepartmentIds(exam.getDepartments().stream().map(d -> d.getId()).toList());
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
        dto.setPassScore(exam.getPassScore());
        dto.setMaxScore(exam.getMaxScore());
        dto.setQuestionsCount(exam.getQuestionsCount() != null ? exam.getQuestionsCount() : exam.getTotalQuestions());
        dto.setIsPublic(exam.getIsPublic());
        dto.setStatus(exam.getStatus());
        dto.setVisibility(exam.getVisibility());
        dto.setStartAt(exam.getStartAt());
        dto.setEndAt(exam.getEndAt());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setModifiedAt(exam.getModifiedAt());

        // Lấy thông tin người tạo
        if (exam.getCreatedBy() != null) {
            dto.setUser(userMapper.toDTO(exam.getCreatedBy()));
        }

        // Lấy thông tin lớp học - môn học chi tiết
        if (exam.getClassSubject() != null) {
            dto.setClassSubject(classSubjectMapper.toFullDTO(exam.getClassSubject()));
        }

        if (exam.getSubject() != null) {
            dto.setSubject(subjectMapper.toDTO(exam.getSubject()));
        }

        // Map departments to list of ids
        if (exam.getDepartments() != null && !exam.getDepartments().isEmpty()) {
            dto.setDepartmentIds(exam.getDepartments().stream().map(d -> d.getId()).toList());
        }

        // Map majors to list of ids
        if (exam.getMajors() != null && !exam.getMajors().isEmpty()) {
            dto.setMajorIds(exam.getMajors().stream().map(m -> m.getId()).toList());
        }

        if (exam.getChapters() != null && !exam.getChapters().isEmpty()) {
            dto.setChapterIds(exam.getChapters().stream().map(Chapter::getId).toList());
            dto.setChapters(exam.getChapters().stream().map(chapterMapper::toDTO).toList());
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

        if (dto.getVisibility() != null) {
            try {
                exam.setVisibility(ExamVisibility.valueOf(dto.getVisibility().name())); 
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
            }
        } 

        if (dto.getChapterIds() != null && !dto.getChapterIds().isEmpty()) {
            exam.setChapters(new HashSet<>(chapterRepository.findAllById(dto.getChapterIds())));
        }

        return exam;
    }
    
}
