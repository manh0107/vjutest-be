package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.Model.Result;

@Component
public class ResultMapper {
    private final UserMapper userMapper;
    private final ExamMapper examMapper;

    @Autowired
    public ResultMapper(UserMapper userMapper, ExamMapper examMapper) {
        this.userMapper = userMapper;
        this.examMapper = examMapper;
    }

    public ResultDTO toSimpleDTO(Result result) {
        if (result == null) {
            return null;
        }

        ResultDTO dto = new ResultDTO();
        dto.setId(result.getId());
        
        if(result.getExam() != null) {
            dto.setExamId(result.getExam().getId());
            dto.setExamName(result.getExam().getName());
        }
        if(result.getUser() != null) {
            dto.setUserId(result.getUser().getId());
            dto.setUserName(result.getUser().getName());
        }
        
        dto.setScore(result.getScore());
        dto.setSubmitTime(result.getSubmitTime());
        dto.setStartTime(result.getStartTime());
        dto.setEndTime(result.getEndTime());
        dto.setIsSubmitted(result.getIsSubmitted());
        dto.setIsPassed(result.getIsPassed());

        return dto;
    }

    public ResultDTO toFullDTO(Result result) {
        if (result == null) {
            return null;
        }

        ResultDTO dto = new ResultDTO();
        dto.setId(result.getId());
        
        if(result.getExam() != null) {
            dto.setExam(examMapper.toFullDTO(result.getExam()));
            dto.setExamCode(result.getExam().getExamCode());
            dto.setExamName(result.getExam().getName());
            dto.setSubjectName(result.getExam().getSubject() != null ? result.getExam().getSubject().getName() : null);
            // Nếu Exam không có getChapter(), bỏ qua hoặc lấy từ classSubject nếu có
            // dto.setChapterName(result.getExam().getChapter() != null ? result.getExam().getChapter().getName() : null);
        }
        if(result.getUser() != null) {
            dto.setUser(userMapper.toDTO(result.getUser()));
            dto.setStudentName(result.getUser().getName());
            dto.setStudentCode(result.getUser().getCode() != null ? result.getUser().getCode().toString() : null);
            dto.setStudentAvatar(result.getUser().getImageUrl());
        }
        
        dto.setScore(result.getScore());
        dto.setSubmitTime(result.getSubmitTime());
        dto.setStartTime(result.getStartTime());
        dto.setEndTime(result.getEndTime());
        dto.setIsSubmitted(result.getIsSubmitted());
        dto.setIsPassed(result.getIsPassed());

        return dto;
    }

    public Result toEntity(ResultDTO dto) {
        if (dto == null) {
            return null;
        }
    
        Result result = new Result();
        result.setId(dto.getId());
        result.setScore(dto.getScore());
        result.setIsSubmitted(dto.getIsSubmitted());
        result.setIsPassed(dto.getIsPassed());
    
        return result;
    }
}
