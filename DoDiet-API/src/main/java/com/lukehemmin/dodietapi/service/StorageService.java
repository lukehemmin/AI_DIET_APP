package com.lukehemmin.dodietapi.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir);
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("meals"));
            Files.createDirectories(rootLocation.resolve("profiles"));
            log.info("Storage initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String store(MultipartFile file, String subDir, String userId) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file " + filename);
        }
        if (filename.contains("..")) {
            // This is a security check
            throw new RuntimeException(
                    "Cannot store file with relative path outside current directory "
                            + filename);
        }

        try {
            // Generate a unique filename
            String extension = "";
            int i = filename.lastIndexOf('.');
            if (i > 0) {
                extension = filename.substring(i);
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // Create directory for user if not exists (optional, or just flat structure)
            // For now, let's put everything in meals/ or specific subDir
            Path targetDir = this.rootLocation.resolve(subDir);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetPath = targetDir.resolve(newFilename);
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Return the relative path or URL
            // For simplicity, we return the path relative to upload dir, 
            // e.g., "meals/uuid.jpg"
            return subDir + "/" + newFilename;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }
    
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * 프로필 이미지 저장
     */
    public String storeProfileImage(MultipartFile file, String userId) {
        return store(file, "profiles", userId);
    }
}
