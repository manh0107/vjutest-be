package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.JoinRequestDTO;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.ClassEntity.VisibilityScope;

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

    public ClassEntityDTO toDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setVisibility(entity.getVisibility());

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByName(entity.getCreatedBy().getName());
            dto.setCreateByImage(entity.getCreatedBy().getImage());
        }

        // Map majors to list of ids
        if (entity.getMajors() != null && !entity.getMajors().isEmpty()) {
            dto.setMajorIds(entity.getMajors().stream().map(m -> m.getId()).toList());
        }

        // Map departments to list of ids
        if (entity.getDepartments() != null && !entity.getDepartments().isEmpty()) {
            dto.setDepartmentIds(entity.getDepartments().stream().map(d -> d.getId()).toList());
        }

        if (entity.getUsers() != null) {
            dto.setUserImage(entity.getUsers().stream().findFirst().map(User::getImage).orElse(null));
        }

        if (entity.getTeachers() != null) {
            dto.setTeacherImage(entity.getTeachers().stream().findFirst().map(User::getImage).orElse(null));
        }

        List<JoinRequestDTO> joinRequestDTOs = (entity.getJoinRequests() != null) ? 
            entity.getJoinRequests().stream()
                .map(joinRequestMapper::toFullDTO)
                .collect(Collectors.toList()) 
            : null;
        dto.setJoinRequests(joinRequestDTOs);

        List<UserDTO> teacherDTOs = (entity.getTeachers() != null) ? 
            entity.getTeachers().stream() 
                .map(userMapper::toDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setTeachers(teacherDTOs);

        List<UserDTO> userDTOs = (entity.getUsers() != null) ? 
            entity.getUsers().stream()
                .map(userMapper::toDTO) 
                .collect(Collectors.toList()) 
            : null;
        dto.setUsers(userDTOs);

        return dto;
    }

    public ClassEntity toEntity(ClassEntityDTO dto) {
        ClassEntity entity = new ClassEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setClassCode(dto.getClassCode());
        entity.setDescription(dto.getDescription());
        entity.setCreatedAt(dto.getCreatedAt());

         if (dto.getVisibility() != null) {
            try {
                entity.setVisibility(VisibilityScope.valueOf(dto.getVisibility().name())); 
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getVisibility());
            }
        } else {
            entity.setVisibility(VisibilityScope.PUBLIC);

        }

        return entity;
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

         // Map majors to list of ids
         if (entity.getMajors() != null && !entity.getMajors().isEmpty()) {
            dto.setMajorIds(entity.getMajors().stream().map(m -> m.getId()).toList());
        }

        // Map departments to list of ids
        if (entity.getDepartments() != null && !entity.getDepartments().isEmpty()) {
            dto.setDepartmentIds(entity.getDepartments().stream().map(d -> d.getId()).toList());
        }

        return dto;
    }

}
