package com.example.vjutest.DTO;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.vjutest.Model.Subject.VisibilityScope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectDTO {
    private Long id;
    private String name;
    private String subjectCode;
    private String description;
    private Integer creditHour;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long modifiedById;
    private String modifiedByName;
    private VisibilityScope visibility;
    private List<Long> majorIds;
    private List<Long> departmentIds;
}
