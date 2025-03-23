package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private Long createdBy;
    private List<?> users;
    private List<?> teachers;
    private List<JoinRequestDTO> joinRequests;
}