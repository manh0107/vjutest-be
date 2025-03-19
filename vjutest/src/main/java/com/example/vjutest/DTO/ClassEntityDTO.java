package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class ClassEntityDTO {
    private Long id;
    private String name;
    private String classCode;
    private String description;
    private Long createdById;
    private List<?> users;
}