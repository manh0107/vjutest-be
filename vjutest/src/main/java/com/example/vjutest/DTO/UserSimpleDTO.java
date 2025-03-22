package com.example.vjutest.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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