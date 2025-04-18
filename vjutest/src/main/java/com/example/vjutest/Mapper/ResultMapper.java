package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.Model.Result.PassTest;
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
        dto.setSubmittedAt(result.getSubmittedAt());
        dto.setDurationTime(result.getDurationTime());
        dto.setPassTest(result.getPassTest() != null ? result.getPassTest().toString() : null);
        dto.setStartedAt(result.getStartedAt());
        dto.setEndedAt(result.getEndedAt());
        dto.setIsSubmitted(result.getIsSubmitted());

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
        }
        if(result.getUser() != null) {
            dto.setUser(userMapper.toDTO(result.getUser()));
        }
        
        dto.setScore(result.getScore());
        dto.setSubmittedAt(result.getSubmittedAt());
        dto.setDurationTime(result.getDurationTime());
        dto.setPassTest(result.getPassTest() != null ? result.getPassTest().toString() : null);
        dto.setStartedAt(result.getStartedAt());
        dto.setEndedAt(result.getEndedAt());
        dto.setIsSubmitted(result.getIsSubmitted());

        return dto;
    }

    public Result toEntity(ResultDTO dto) {
        if (dto == null) {
            return null;
        }
    
        Result result = new Result();
        result.setId(dto.getId());

        if (dto.getPassTest() != null) {
            try {
                result.setPassTest(PassTest.valueOf(dto.getPassTest())); 
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getPassTest());
            }
        } else {
            result.setPassTest(PassTest.FAIL); 
        }
    
        return result;
    }
}
