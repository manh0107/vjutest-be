package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Service.ClassEntityService;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
public class ClassEntityController {

    private final ClassEntityService classEntityService;
    private final ClassEntityRepository classEntityRepository;

    @Autowired
    public ClassEntityController(ClassEntityService classEntityService, ClassEntityRepository classEntityRepository) {
        this.classEntityService = classEntityService;
        this.classEntityRepository = classEntityRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestParam Long userId, @RequestBody ClassEntity classEntity) {
        try {
            ClassEntity createdClass = classEntityService.createClass(classEntity
                    .getName(), classEntity.getClassCode(), classEntity.getDescription(), userId);
            return ResponseEntity.ok(createdClass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllClasses() {
        List<ClassEntity> classes = classEntityRepository.findAll();
        List<ClassEntityDTO> classDTOs = classes.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(classDTOs);
    }

    private ClassEntityDTO convertToDTO(ClassEntity entity) {
        ClassEntityDTO dto = new ClassEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setClassCode(entity.getClassCode());
        dto.setDescription(entity.getDescription());
        // Only include the user ID or necessary user fields, not the entire user object
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
        }
        return dto;
    }
}
