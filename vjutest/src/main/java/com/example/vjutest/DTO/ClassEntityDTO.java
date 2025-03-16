package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClassEntityDTO {
    private Long id;
    private String name;
    private String classCode;
    private String description;
    private Long createdById;
}