package com.example.vjutest.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequestDTO {
    private Long id;
    private Long userId;
    private Long classId;
    private String status;
    private UserDTO user;
    private String type;
}
