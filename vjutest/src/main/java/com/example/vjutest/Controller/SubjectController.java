package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Mapper.SubjectMapper;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Service.SubjectService;
import com.example.vjutest.User.CustomUserDetails;

@RestController
@RequestMapping("/subjects")
@CrossOrigin(origins = "*")
public class SubjectController {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;

    @Autowired
    public SubjectController(SubjectService subjectService, SubjectMapper subjectMapper) {
        this.subjectService = subjectService;
        this.subjectMapper = subjectMapper;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<?> createSubject(Authentication authentication, @RequestBody SubjectDTO subjectDTO) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        try {
            Subject createdSubject = subjectService.createSubject(
                subjectDTO.getName(),
                subjectDTO.getSubjectCode(),
                subjectDTO.getDescription(),
                subjectDTO.getCreditHour(),
                userId,
                subjectDTO.getMajorIds(),
                subjectDTO.getDepartmentIds(),
                subjectDTO.getVisibility()
            );
            return ResponseEntity.ok(subjectMapper.toDTO(createdSubject));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/all")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<Subject> subjects = subjectService.getAllSubjects(userId);
        List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subjectMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjectDTOs);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/find/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return subjectService.getSubjectById(id, userId)
                .map(subject -> ResponseEntity.ok(subjectMapper.toDTO(subject)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Long id, @RequestBody SubjectDTO subjectDTO, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        try {
            Subject updatedSubject = subjectService.updateSubject(
                id,
                subjectDTO.getName(),
                subjectDTO.getSubjectCode(),
                subjectDTO.getDescription(),
                subjectDTO.getCreditHour(),
                userId,
                subjectDTO.getMajorIds(),
                subjectDTO.getDepartmentIds(),
                subjectDTO.getVisibility()
            );
            return ResponseEntity.ok(subjectMapper.toDTO(updatedSubject));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            subjectService.deleteSubject(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
