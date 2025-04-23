package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    private List<?> users;
    private List<?> teachers;
    private List<JoinRequestDTO> joinRequests;
}