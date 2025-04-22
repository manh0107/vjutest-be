package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSimpleDTO {
    private Long id;
    private String name;

    // Constructor accepting Long
    public UserSimpleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter and setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and setter for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Existing fields and methods
}