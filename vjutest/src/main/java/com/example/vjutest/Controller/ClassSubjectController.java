package com.example.vjutest.Controller;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.ClassSubject;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Service.ClassSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/class-subjects")
public class ClassSubjectController {

    private ClassEntityRepository classEntityRepository;
    private SubjectRepository subjectRepository;
    private ClassSubjectService classSubjectService;

    @Autowired
    public ClassSubjectController(ClassEntityRepository classEntityRepository, SubjectRepository subjectRepository, ClassSubjectService classSubjectService) {
        this.classEntityRepository = classEntityRepository;
        this.subjectRepository = subjectRepository;
        this.classSubjectService = classSubjectService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    @PostMapping("/{classId}/{subjectId}/upload")
    public String uploadDocument(
            @PathVariable Long classId, 
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") String folderId) throws IOException, GeneralSecurityException {
    
        // Lấy classEntity và subject từ database
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));
    
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
    
        // Upload file
        ClassSubject classSubject = classSubjectService.uploadDocument(classEntity, subject, file, folderId);
    
        return "File uploaded: " + classSubject.getDocumentUrl();
    }
    
}
