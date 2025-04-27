package com.example.vjutest.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.example.vjutest.DTO.ClassEntityDTO;
import com.example.vjutest.DTO.UserDTO;
import com.example.vjutest.DTO.SubjectDTO;
import com.example.vjutest.Mapper.ClassEntityMapper;
import com.example.vjutest.Mapper.SubjectMapper;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.ClassEntity.VisibilityScope;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Service.ClassEntityService;

@RestController
@RequestMapping("/api/classes")
public class ClassEntityController {

    private final ClassEntityService classEntityService;
    private final ClassEntityMapper classEntityMapper;
    private final SubjectMapper subjectMapper;

    @Autowired
    public ClassEntityController(ClassEntityService classEntityService, ClassEntityMapper classEntityMapper, SubjectMapper subjectMapper) {
        this.classEntityService = classEntityService;
        this.classEntityMapper = classEntityMapper;
        this.subjectMapper = subjectMapper;
    }

    @PostMapping
    public ResponseEntity<ClassEntityDTO> createClass(
            @RequestParam String name,
            @RequestParam String classCode,
            @RequestParam String description,
            @RequestParam Long userId,
            @RequestParam List<Long> departmentIds,
            @RequestParam List<Long> majorIds) {
        ClassEntity createdClass = classEntityService.createClass(name, classCode, description, userId, departmentIds, majorIds);
        return ResponseEntity.ok(classEntityMapper.toDTO(createdClass));
    }

    @GetMapping
    public ResponseEntity<List<ClassEntityDTO>> getAllClasses(@RequestParam Long userId) {
        List<ClassEntity> classes = classEntityService.getAllClasses(userId);
        return ResponseEntity.ok(classes.stream()
                .map(classEntityMapper::toDTO)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ClassEntityDTO>> getClassesByDepartment(
            @PathVariable Long departmentId,
            @RequestParam Long userId) {
        List<ClassEntity> classes = classEntityService.getClassesByDepartment(departmentId, userId);
        return ResponseEntity.ok(classes.stream()
                .map(classEntityMapper::toDTO)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/major/{majorId}")
    public ResponseEntity<List<ClassEntityDTO>> getClassesByMajor(
            @PathVariable Long majorId,
            @RequestParam Long userId) {
        List<ClassEntity> classes = classEntityService.getClassesByMajor(majorId, userId);
        return ResponseEntity.ok(classes.stream()
                .map(classEntityMapper::toDTO)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassEntityDTO> getClassById(@PathVariable Long id, @RequestParam Long userId) {
        Optional<ClassEntity> classOpt = classEntityService.getClassById(id, userId);
        return classOpt.map(classEntity -> ResponseEntity.ok(classEntityMapper.toDTO(classEntity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassEntityDTO> updateClass(
            @PathVariable Long id,
            @RequestBody ClassEntityDTO classDTO,
            @RequestParam Long userId) {
        ClassEntity updatedClass = classEntityService.updateClass(
            id,
            classEntityMapper.toEntity(classDTO),
            userId,
            classDTO.getDepartmentIds(),
            classDTO.getMajorIds()
        );
        return ResponseEntity.ok(classEntityMapper.toDTO(updatedClass));
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<ClassEntityDTO> changeVisibility(
            @PathVariable Long id,
            @RequestParam VisibilityScope visibility,
            @RequestParam Long userId) {
        ClassEntity updatedClass = classEntityService.changeVisibility(id, visibility, userId);
        return ResponseEntity.ok(classEntityMapper.toDTO(updatedClass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id, @RequestParam Long userId) {
        classEntityService.deleteClass(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> addStudentToClass(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestParam Long userId) {
        classEntityService.addStudentToClass(classId, studentId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestParam Long userId) {
        classEntityService.removeStudentFromClass(classId, studentId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{classId}/join")
    public ResponseEntity<String> requestToJoin(
            @PathVariable Long classId,
            @RequestParam Long studentId) {
        String message = classEntityService.requestToJoin(studentId, classId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{classId}/teachers/{inviteeId}")
    public ResponseEntity<Void> inviteTeacher(
            @PathVariable Long classId,
            @PathVariable Long inviteeId,
            @RequestParam Long inviterId) {
        classEntityService.inviteTeacher(classId, inviterId, inviteeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{classId}/leave")
    public ResponseEntity<String> leaveClass(
            @PathVariable Long classId,
            @RequestParam Long studentId) {
        String message = classEntityService.leaveClass(classId, studentId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{classId}/students")
    public ResponseEntity<List<UserDTO>> getStudentsInClass(@PathVariable Long classId) {
        List<UserDTO> students = classEntityService.getStudentsInClass(classId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{classId}/teachers")
    public ResponseEntity<List<UserDTO>> getTeachersInClass(@PathVariable Long classId) {
        List<UserDTO> teachers = classEntityService.getTeachersInClass(classId);
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/{classId}/subjects/{subjectId}")
    public ResponseEntity<Void> addSubjectToClass(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @RequestParam Long userId) {
        classEntityService.addSubjectToClass(classId, subjectId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{classId}/subjects/{subjectId}")
    public ResponseEntity<Void> removeSubjectFromClass(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @RequestParam Long userId) {
        classEntityService.removeSubjectFromClass(classId, subjectId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{classId}/subjects")
    public ResponseEntity<List<SubjectDTO>> getSubjectsInClass(@PathVariable Long classId) {
        List<Subject> subjects = classEntityService.getSubjectsInClass(classId);
        List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subjectMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjectDTOs);
    }
}
