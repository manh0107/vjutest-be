package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.JoinRequestDTO;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.UserSimpleDTO;
import com.example.vjutest.Model.ClassEntity;

import java.util.List;
import java.util.stream.Collectors;
import com.example.vjutest.Model.User; // Ensure the correct package path for User

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ClassEntityMapper {

    private final UserMapper userMapper;
    private final JoinRequestMapper joinRequestMapper;

    @Autowired
    public ClassEntityMapper(UserMapper userMapper, JoinRequestMapper joinRequestMapper) {
        this.userMapper = userMapper;
        this.joinRequestMapper = joinRequestMapper;
    }

    //Lấy đầy đủ thông tin
    public ClassEntityDTO toFullDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByName(entity.getCreatedBy().getName());
            dto.setCreateByImage(entity.getCreatedBy().getImage());
        }
        if (entity.getUsers() != null) {
            dto.setUserImage(entity.getUsers().stream().findFirst().map(User::getImage).orElse(null));
        }
        if (entity.getTeachers() != null) {
            dto.setTeacherImage(entity.getTeachers().stream().findFirst().map(User::getImage).orElse(null));
        }

        //Lấy đầy đủ thông tin JoinRequest + User gửi request
        List<JoinRequestDTO> joinRequestDTOs = (entity.getJoinRequests() != null) ? 
            entity.getJoinRequests().stream()
                .map(joinRequestMapper::toFullDTO)  // Lấy cả thông tin User gửi request
                .collect(Collectors.toList()) 
            : null;
        dto.setJoinRequests(joinRequestDTOs); 

        //Lấy danh sách giáo viên (Teachers)
        List<UserDTO> teacherDTOs = (entity.getTeachers() != null) ? 
            entity.getTeachers().stream() 
                .map(userMapper::toDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setTeachers(teacherDTOs);  

        //Lấy danh sách học sinh (Students)
        List<UserDTO> userDTOs = (entity.getUsers() != null) ? 
            entity.getUsers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setUsers(userDTOs);  

        return dto;
    }

    //Chỉ lấy ID đơn giản
    public ClassEntityDTO toSimpleDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByName(entity.getCreatedBy().getName());
        }

        //Chỉ lấy ID của JoinRequest
        dto.setJoinRequests((entity.getJoinRequests() != null) ? 
            entity.getJoinRequests().stream()
                .map(joinRequestMapper::toSimpleDTO) 
                .collect(Collectors.toList()) 
            : null);

        //Lấy danh sách giáo viên (Teachers id)
        List<UserSimpleDTO> teacherDTOs = (entity.getTeachers() != null) ? 
            entity.getTeachers().stream() 
                .map(userMapper::toSimpleDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setTeachers(teacherDTOs); 

        //Lấy danh sách học sinh đơn giản (chỉ ID)
        List<UserSimpleDTO> userDTOs = (entity.getUsers() != null) ? 
            entity.getUsers().stream()
                .map(userMapper::toSimpleDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setUsers(userDTOs);

        return dto;
    }
}
