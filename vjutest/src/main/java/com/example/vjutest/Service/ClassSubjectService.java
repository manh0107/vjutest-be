package com.example.vjutest.Service;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.ClassSubject;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Repository.ClassSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class ClassSubjectService {

    @Autowired
    private ClassSubjectRepository classSubjectRepository;

    @Autowired
    private GoogleDriveService googleDriveService;

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không hợp lệ.");
        }
    
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_file";
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
    
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        } catch (Exception e) {
            throw new IOException("Lỗi khi chuyển đổi MultipartFile sang File: " + e.getMessage());
        }
    
        return convFile;
    }
    
    public ClassSubject uploadDocument(ClassEntity classEntity, Subject subject, MultipartFile file, String parentFolderId) 
        throws IOException, GeneralSecurityException {

        // Kiểm tra input
        if (classEntity == null || subject == null || file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu đầu vào không hợp lệ.");
        }

        // Lấy ClassSubject hoặc tạo mới
        ClassSubject existingClassSubject = classSubjectRepository.findByClassEntityAndSubject(classEntity, subject);
        String folderId;

        if (existingClassSubject != null && existingClassSubject.getGoogleDriveFolderId() != null) {
            folderId = existingClassSubject.getGoogleDriveFolderId();
        } else {
            folderId = googleDriveService.createFolder(classEntity.getName() + "-" + subject.getName(), parentFolderId);

            if (existingClassSubject == null) {
                existingClassSubject = new ClassSubject();
                existingClassSubject.setClassEntity(classEntity);
                existingClassSubject.setSubject(subject);
            }

            existingClassSubject.setGoogleDriveFolderId(folderId);
            classSubjectRepository.save(existingClassSubject);
        }

        // Convert file
        File tempFile = convertMultiPartToFile(file);
        String documentUrl = null;

        try {
            String uploadResult = googleDriveService.uploadFile(tempFile, folderId);
            String[] parts = uploadResult.split("\\|\\|");
            documentUrl = parts[0];
            String fileName = parts.length > 1 ? parts[1] : null;
            existingClassSubject.setFileName(fileName);
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload file lên Google Drive: " + e.getMessage());
        } finally {
            tempFile.delete(); // Xóa file tạm
        }

        if (documentUrl == null) {
            throw new IOException("Không thể tải lên Google Drive.");
        }

        // Lưu URL file vào DB
        existingClassSubject.setDocumentUrl(documentUrl);
        return classSubjectRepository.save(existingClassSubject);
    }
}