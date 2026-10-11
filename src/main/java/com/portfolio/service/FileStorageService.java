package com.portfolio.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${portfolio.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + root, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot).toLowerCase();
        }
        String storedName = UUID.randomUUID() + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + original, e);
        }
        return storedName;
    }

    public String storeBytes(byte[] data, String extension) {
        if (data == null || data.length == 0) {
            return null;
        }
        String ext = (extension == null || extension.isBlank()) ? "" : extension;
        if (!ext.isEmpty() && !ext.startsWith(".")) {
            ext = "." + ext;
        }
        String storedName = UUID.randomUUID() + ext;
        try {
            Files.write(root.resolve(storedName), data);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store generated file", e);
        }
        return storedName;
    }

    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(root.resolve(storedName));
        } catch (IOException ignored) {
        }
    }

    public Path load(String storedName) {
        return root.resolve(storedName).normalize();
    }
}
