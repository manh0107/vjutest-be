package com.example.vjutest.Mapper;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.UserSimpleDTO;
import com.example.vjutest.Model.ClassEntity;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ClassEntityMapper {

    private UserMapper userMapper;

    @Autowired
    public ClassEntityMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 🔹 Dùng khi lấy chi tiết lớp học (Hiển thị đầy đủ thông tin user)
    public ClassEntityDTO toFullDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
        }

        // Lấy đầy đủ thông tin user
        List<UserDTO> userDTOs = (entity.getUsers() != null) ? 
            entity.getUsers().stream()
                .map(userMapper::toDTO) // Lấy đầy đủ thông tin của user
                .collect(Collectors.toList()) 
            : null;
        dto.setUsers(userDTOs);

        return dto;
    }

    public ClassEntityDTO toSimpleDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
        }

        // Chỉ lấy danh sách UserSimpleDTO thay vì UserDTO đầy đủ
        List<UserSimpleDTO> userDTOs = (entity.getUsers() != null) ? 
            entity.getUsers().stream()
                .map(userMapper::toSimpleDTO) // Chỉ lấy thông tin cần thiết
                .collect(Collectors.toList()) 
            : null;
        dto.setUsers(userDTOs);
        return dto;
    }
}
