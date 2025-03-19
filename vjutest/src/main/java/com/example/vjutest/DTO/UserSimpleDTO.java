package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSimpleDTO {
    private Long id;

    // Constructor accepting Long
    public UserSimpleDTO(Long id) {
        this.id = id;
    }

    // Getter and setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Existing fields and methods
}