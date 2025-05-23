package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.vjutest.Model.ClassEntity.VisibilityScope;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassEntityDTO {
    private Long id;
    private String name;
    private String classCode;
    private String description;
    private Long createdById;
    private String createdByName;
    private String createByImage;
    private String userImage;
    private String teacherImage;
    private LocalDateTime createdAt;
    private VisibilityScope visibility;
    private List<Long> departmentIds;
    private List<Long> majorIds;
    private List<UserDTO> users;
    private List<UserDTO> teachers;
    private List<JoinRequestDTO> joinRequests;
}