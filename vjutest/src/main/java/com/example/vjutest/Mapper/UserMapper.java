package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.UserSimpleDTO;
import com.example.vjutest.Model.User;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;

    @Autowired
    public UserMapper(DepartmentMapper departmentMapper, MajorMapper majorMapper) {
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setCode(user.getCode());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender());
        dto.setEmail(user.getEmail());
        dto.setImage(user.getImage());
        dto.setRole(user.getRole().getName());
        dto.setDepartment(departmentMapper.toDTO(user.getDepartment()));
        dto.setMajor(majorMapper.toDTO(user.getMajor()));
        dto.setIsEnabled(user.getIsEnabled());
        
        // Map collections
        dto.setCreateDepartments(convertToIdList(user.getCreatedDepartments()));
        dto.setCreateMajors(convertToIdList(user.getCreatedMajors()));
        dto.setCreateClasses(convertToIdList(user.getCreateClasses()));
        dto.setCreateSubjects(convertToIdList(user.getCreateSubjects()));
        dto.setCreatedExams(convertToIdList(user.getCreatedExams()));
        dto.setCreatedQuestions(convertToIdList(user.getCreatedQuestions()));
        dto.setClasses(convertToIdList(user.getClasses()));
        dto.setTeacherOfClasses(convertToIdList(user.getTeacherOfClasses()));
        dto.setJoinRequests(convertToIdList(user.getJoinRequests()));
        dto.setUserAnswers(convertToIdList(user.getUserAnswers()));
        dto.setCreatedAt(user.getCreatedAt());
        
        return dto;
    }

    public UserSimpleDTO toSimpleDTO(User user) {
        return new UserSimpleDTO(
            user.getId(),
            user.getName()
        );
    }

    // Hàm hỗ trợ để giữ nguyên null hoặc lấy danh sách ID
    private List<Long> convertToIdList(Collection<?> entities) {
        return (entities != null) ? 
                entities.stream()
                        .map(entity -> {
                            try {
                                return (Long) entity.getClass().getMethod("getId").invoke(entity);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(id -> id != null)
                        .collect(Collectors.toList()) 
                : null; // Trả về null nếu danh sách gốc là null
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setCode(dto.getCode());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setGender(dto.getGender());
        user.setEmail(dto.getEmail());
        user.setImage(dto.getImage());
        user.setIsEnabled(dto.getIsEnabled());
        
        // Department and Major will be set in the service layer
        // to ensure all required fields are properly set
        
        return user;
    }

}
