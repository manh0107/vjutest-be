package com.example.vjutest.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.vjutest.DTO.JoinRequestDTO;
import com.example.vjutest.Model.JoinRequest;

@Component
public class JoinRequestMapper {

    private final UserMapper userMapper;

    @Autowired
    public JoinRequestMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    //Chỉ lấy ID của User
    public JoinRequestDTO toSimpleDTO(JoinRequest joinRequest) {
        if (joinRequest == null) {
            return null;
        }

        JoinRequestDTO dto = new JoinRequestDTO();
        dto.setId(joinRequest.getId());
        dto.setStatus(joinRequest.getStatus().toString());
        dto.setType(joinRequest.getType().toString());
        dto.setClassId(joinRequest.getClassEntity().getId());
        dto.setUserId(joinRequest.getUser().getId()); // Chỉ lấy ID của User
        return dto;
    }

    //Lấy đầy đủ thông tin User
    public JoinRequestDTO toFullDTO(JoinRequest joinRequest) {
        if (joinRequest == null) {
            return null;
        }

        JoinRequestDTO dto = new JoinRequestDTO();
        dto.setId(joinRequest.getId());
        dto.setStatus(joinRequest.getStatus().toString());
        dto.setType(joinRequest.getType().toString());
        dto.setClassId(joinRequest.getClassEntity().getId());
        dto.setUser(userMapper.toDTO(joinRequest.getUser())); // Lấy full User
        return dto;
    }

    // Convert DTO to JoinRequest entity
    public JoinRequest toEntity(JoinRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(dto.getId());
        joinRequest.setStatus(JoinRequest.Status.valueOf(dto.getStatus()));
        joinRequest.setType(JoinRequest.Type.valueOf(dto.getStatus()));
        // Note: You need to fetch and set the User and ClassEntity objects from the database
        return joinRequest;
    }
}
