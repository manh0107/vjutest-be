package com.example.vjutest.DTO;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MajorDTO {
    private long id;
    private String name;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long modifiedById;
    private String modifiedByName;
    private DepartmentDTO department;
    private List<SubjectDTO> subjects;
    private Long departmentId;
}
