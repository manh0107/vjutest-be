package com.example.vjutest.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.DepartmentDTO;
import com.example.vjutest.Mapper.DepartmentMapper;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Service.DepartmentService;
import com.example.vjutest.User.CustomUserDetails;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper;

    @Autowired
    public DepartmentController(DepartmentService departmentService, DepartmentMapper departmentMapper) {
        this.departmentService = departmentService;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departmentMapper.toDTOList(departments));
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id)
                .map(department -> ResponseEntity.ok(departmentMapper.toDTO(department)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DepartmentDTO> createDepartment(
            @RequestBody DepartmentDTO departmentDTO,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Department department = departmentService.createDepartment(
                departmentDTO.getName(),
                userId
        );
        return ResponseEntity.ok(departmentMapper.toDTO(department));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO departmentDTO,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Department department = departmentService.updateDepartment(
                id,
                departmentDTO.getName(),
                userId
        );
        return ResponseEntity.ok(departmentMapper.toDTO(department));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        departmentService.deleteDepartment(id, userId);
        return ResponseEntity.ok().build();
    }
} 