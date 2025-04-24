package com.example.vjutest.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.MajorDTO;
import com.example.vjutest.Mapper.MajorMapper;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Service.MajorService;
import com.example.vjutest.User.CustomUserDetails;

@RestController
@RequestMapping("/majors")
@CrossOrigin(origins = "*")
public class MajorController {

    private final MajorService majorService;
    private final MajorMapper majorMapper;

    @Autowired
    public MajorController(MajorService majorService, MajorMapper majorMapper) {
        this.majorService = majorService;
        this.majorMapper = majorMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<MajorDTO>> getAllMajors() {
        List<Major> majors = majorService.getAllMajors();
        return ResponseEntity.ok(majors.stream()
                                       .map(majorMapper::toDTO)
                                       .toList());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<MajorDTO>> getMajorsByDepartment(@PathVariable Long departmentId) {
        List<Major> majors = majorService.getMajorsByDepartment(departmentId);
        return ResponseEntity.ok(majors.stream()
                                       .map(majorMapper::toDTO)
                                       .toList());
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<MajorDTO> getMajorById(@PathVariable Long id) {
        return majorService.getMajorById(id)
                .map(major -> ResponseEntity.ok(majorMapper.toDTO(major)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MajorDTO> createMajor(
            @RequestBody MajorDTO majorDTO,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        Major major = majorService.createMajor(
                majorDTO.getName(),
                majorDTO.getDepartmentId(),
                userId
        );
        return ResponseEntity.ok(majorMapper.toDTO(major));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MajorDTO> updateMajor(
            @PathVariable Long id,
            @RequestBody MajorDTO majorDTO,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        Major major = majorService.updateMajor(
                id,
                majorDTO.getName(),
                majorDTO.getDepartmentId(),
                userId
        );
        return ResponseEntity.ok(majorMapper.toDTO(major));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteMajor(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        majorService.deleteMajor(id, userId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
