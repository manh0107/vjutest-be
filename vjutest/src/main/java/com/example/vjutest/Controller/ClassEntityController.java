package com.example.vjutest.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestParam Long userId, @RequestBody ClassEntity classEntity) {
        try {
            ClassEntity createdClass = classEntityService.createClass(classEntity
                    .getName(), classEntity.getClassCode(), classEntity.getDescription(), userId);
            return ResponseEntity.ok(classEntityMapper.toFullDTO(createdClass));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ClassEntityDTO>> getAllClasses() {
        List<ClassEntity> classes = classEntityService.getAllClasses();
        List<ClassEntityDTO> classDTOs = classes.stream()
                .map(classEntityMapper::toSimpleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classDTOs);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Long id) {
        return classEntityService.getClassById(id)
                .map(classEntity -> ResponseEntity.ok(classEntityMapper.toFullDTO(classEntity)))
                .orElse(ResponseEntity.notFound().build());
    }

    //Cập nhật lớp học
    @PutMapping("/update/{id}")
    public ResponseEntity<ClassEntityDTO> updateClass(
            @PathVariable Long id,
            @RequestBody ClassEntity classEntity,
            @RequestParam Long userId) {
        ClassEntity updatedClass = classEntityService.updateClass(id, classEntity, userId);
        return ResponseEntity.ok(classEntityMapper.toSimpleDTO(updatedClass));
    }

    //Xóa lớp học
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteClass(@PathVariable Long id, @RequestParam Long userId) {
        classEntityService.deleteClass(id, userId);
        return ResponseEntity.ok("Lớp học đã được xóa!");
    }

    //Thêm sinh viên vào lớp
    @PostMapping("/{classId}/add-student")
    public ResponseEntity<String> addStudentToClass(
            @PathVariable Long classId,
            @RequestParam Long studentId,
            @RequestParam Long userId) {
        classEntityService.addStudentToClass(classId, studentId, userId);
        return ResponseEntity.ok("Học sinh đã được thêm vào lớp!");
    }

    //Mời giáo viên vào lớp
    @PostMapping("/{classId}/invite-teacher")
    public ResponseEntity<String> inviteTeacher(
            @PathVariable Long classId,
            @RequestParam Long inviterId,
            @RequestParam Long inviteeId) {
        classEntityService.inviteTeacher(classId, inviterId, inviteeId);
        return ResponseEntity.ok("Giáo viên đã được mời vào lớp!");
    }

    //Sinh viên yêu cầu tham gia lớp
    @PostMapping("/request")
    public ResponseEntity<?> requestToJoin(@RequestParam Long studentId, @RequestParam Long classId) {
        try {
            String message = classEntityService.requestToJoin(studentId, classId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Sinh viên rời khỏi lớp
    @DeleteMapping("/{classId}/leave/{studentId}")
    public ResponseEntity<String> leaveClass (
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        classEntityService.leaveClass(classId, studentId);
        return ResponseEntity.ok("Bạn đã rời khỏi lớp học thành công!");
    }

     //Xóa sinh viên khỏi lớp
     @DeleteMapping("/{classId}/remove-student")
     public ResponseEntity<String> removeStudentFromClass(
             @PathVariable Long classId,
             @RequestParam Long studentId,
             @RequestParam Long userId) {
         classEntityService.removeStudentFromClass(classId, studentId, userId);
         return ResponseEntity.ok("Học sinh đã được xóa khỏi lớp!");
     }

     //Lấy danh sách sinh viên trong lớp học
    @GetMapping("/{classId}/students")
    public ResponseEntity<List<UserDTO>> getStudentsInClass(@PathVariable Long classId) {
        List<UserDTO> students = classEntityService.getStudentsInClass(classId);
        return ResponseEntity.ok(students);
    }

    //Lấy danh sách giáo viên trong lớp học
    @GetMapping("/{classId}/teachers")
    public ResponseEntity<List<UserDTO>> getTeachersInClass(@PathVariable Long classId) {
        List<UserDTO> teachers = classEntityService.getTeachersInClass(classId);
        return ResponseEntity.ok(teachers);
    }
}
