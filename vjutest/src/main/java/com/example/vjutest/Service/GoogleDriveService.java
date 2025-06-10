package com.example.vjutest.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "VJUTest";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    
    private final String credentialsPath;
    private Drive driveService;

    @Autowired
    public GoogleDriveService(@Value("${GOOGLE_CREDENTIALS_PATH}") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        this.driveService = createDriveService();
    }

    private Drive createDriveService() throws IOException, GeneralSecurityException {
        // Read the service account credentials from the JSON file
        InputStream credentialsStream = new FileInputStream(new java.io.File(credentialsPath));
        
        // Create GoogleCredentials from the service account key file
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(SCOPES);
        
        // Create Drive service
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String uploadFile(java.io.File file, String folderId) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File không hợp lệ hoặc không tồn tại.");
        }
    
        try {
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
    
            // Luôn set parent là folderId mặc định
            String defaultFolderId = "1mrCH-ASy-WV4s61omARxhg7RBsewH9ro";
            fileMetadata.setParents(Collections.singletonList(defaultFolderId));
    
            FileContent mediaContent = new FileContent("application/octet-stream", file);
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, name")
                    .execute();
    
            String documentUrl = uploadedFile.getWebViewLink();
            if (documentUrl != null && documentUrl.contains("/edit")) {
                documentUrl = documentUrl.replace("/edit", "/view");
            }
            String fileName = uploadedFile.getName();
            return documentUrl + "||" + fileName;
        } catch (Exception e) {
            throw new IOException("Lỗi khi upload file lên Google Drive: " + e.getMessage());
        }
    }
    
    public String createFolder(String folderName, String parentFolderId) throws IOException {
        String existingFolderId = getFolderId(folderName, parentFolderId);
        
        if (existingFolderId != null) {
            return existingFolderId; // Trả về ID của thư mục đã tồn tại
        }
    
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
    
        if (parentFolderId != null && !parentFolderId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }
    
        File folder = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
    
        return folder.getId();
    }
    
    // Hàm kiểm tra xem thư mục đã tồn tại chưa
    private String getFolderId(String folderName, String parentFolderId) throws IOException {
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'";
    
        if (parentFolderId != null && !parentFolderId.isEmpty()) {
            query += " and '" + parentFolderId + "' in parents";
        }
    
        List<File> folders = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute()
                .getFiles();
    
        return folders.isEmpty() ? null : folders.get(0).getId();
    }
    
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("URL file không hợp lệ");
        }

        try {
            // Lấy file ID từ URL
            String fileId = extractFileIdFromUrl(fileUrl);
            if (fileId == null) {
                throw new IllegalArgumentException("Không thể lấy file ID từ URL");
            }

            // Xóa file trên Google Drive
            driveService.files().delete(fileId).execute();
        } catch (Exception e) {
            throw new IOException("Lỗi khi xóa file trên Google Drive: " + e.getMessage());
        }
    }

    private String extractFileIdFromUrl(String fileUrl) {
        // URL có dạng: https://drive.google.com/file/d/{fileId}/view?usp=sharing
        String[] parts = fileUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("d") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }
}