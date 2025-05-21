package com.example.vjutest.DTO;

import java.time.LocalDateTime;
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
public class ChapterDTO {
    private Long id;
    private String name;
    private SubjectDTO subject;
    private Long createdById;
    private String createdByName;
    private Long modifiedById;
    private String modifiedByName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long questionTotal;
}
