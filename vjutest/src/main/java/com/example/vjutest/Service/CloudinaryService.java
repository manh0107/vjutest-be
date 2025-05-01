package com.example.vjutest.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "auto"
                ));
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        }
    }

    private String extractPublicId(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/image.jpg
        // We need to extract: folder/image
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2) {
            return null;
        }
        String[] pathParts = parts[1].split("/");
        if (pathParts.length < 2) {
            return null;
        }
        // Remove version prefix if exists
        String publicId = pathParts[pathParts.length - 2] + "/" + pathParts[pathParts.length - 1];
        publicId = publicId.split("\\.")[0]; // Remove file extension
        return publicId;
    }
} 