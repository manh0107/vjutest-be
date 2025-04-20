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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.Mapper.ClassEntityMapper;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Service.ClassEntityService;
import com.example.vjutest.User.CustomUserDetails;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
public class ClassEntityController {

    private final ClassEntityService classEntityService;
    private final ClassEntityMapper classEntityMapper;

    @Autowired
    public ClassEntityController(ClassEntityService classEntityService, ClassEntityMapper classEntityMapper) {
        this.classEntityService = classEntityService;
        this.classEntityMapper = classEntityMapper;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestBody ClassEntity classEntity, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        try {
            ClassEntity createdClass = classEntityService.createClass(
                classEntity.getName(),
                classEntity.getClassCode(),
                classEntity.getDescription(),
                userId
            );
            return ResponseEntity.ok(classEntityMapper.toFullDTO(createdClass));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/all")
    public ResponseEntity<List<ClassEntityDTO>> getAllClasses(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        List<ClassEntity> classes = classEntityService.getAllClasses(userId);
        List<ClassEntityDTO> classDTOs = classes.stream()
                .map(classEntityMapper::toSimpleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classDTOs);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @GetMapping("/find/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Long id, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        return classEntityService.getClassById(id, userId)
                .map(classEntity -> ResponseEntity.ok(classEntityMapper.toFullDTO(classEntity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ClassEntityDTO> updateClass(
            @PathVariable Long id,
            @RequestBody ClassEntity classEntity,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        ClassEntity updatedClass = classEntityService.updateClass(id, classEntity, userId);
        return ResponseEntity.ok(classEntityMapper.toSimpleDTO(updatedClass));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteClass(@PathVariable Long id, Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        classEntityService.deleteClass(id, userId);
        return ResponseEntity.ok("Lớp học đã được xóa!");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @PostMapping("/{classId}/add-student")
    public ResponseEntity<String> addStudentToClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        classEntityService.addStudentToClass(classId, studentId, userId);
        return ResponseEntity.ok("Học sinh đã được thêm vào lớp!");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @PostMapping("/{classId}/invite-teacher")
    public ResponseEntity<String> inviteTeacher(
            @PathVariable Long classId,
            @RequestParam Long inviteeId,
            Authentication authentication) {
        Long inviterId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        classEntityService.inviteTeacher(classId, inviterId, inviteeId);
        return ResponseEntity.ok("Giáo viên đã được mời vào lớp!");
    }

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/request")
    public ResponseEntity<?> requestToJoin(
            @RequestParam Long classId,
            Authentication authentication) {
        Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        try {
            String message = classEntityService.requestToJoin(studentId, classId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_STUDENT')") 
    @DeleteMapping("/{classId}/leave")
    public ResponseEntity<String> leaveClass (
            @PathVariable Long classId,
            Authentication authentication) {
        Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        classEntityService.leaveClass(classId, studentId);
        return ResponseEntity.ok("Bạn đã rời khỏi lớp học thành công!");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @DeleteMapping("/{classId}/remove-student")
    public ResponseEntity<String> removeStudentFromClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        classEntityService.removeStudentFromClass(classId, studentId, userId);
        return ResponseEntity.ok("Học sinh đã được xóa khỏi lớp!");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/{classId}/students")
    public ResponseEntity<List<UserDTO>> getStudentsInClass(@PathVariable Long classId) {
        List<UserDTO> students = classEntityService.getStudentsInClass(classId);
        return ResponseEntity.ok(students);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/{classId}/teachers")
    public ResponseEntity<List<UserDTO>> getTeachersInClass(@PathVariable Long classId) {
        List<UserDTO> teachers = classEntityService.getTeachersInClass(classId);
        return ResponseEntity.ok(teachers);
    }
}
