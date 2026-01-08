package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.exception.ProductResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 로컬 파일 시스템을 사용한 파일 저장 서비스 구현체
 * 개발 환경 및 단일 서버 환경에서 사용
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {
    
    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir;
    
    @Value("${app.upload.max-file-size:5242880}") // 5MB
    private long maxFileSize;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    
    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        
        // 디렉토리별 저장 경로 생성
        Path directoryPath = Paths.get(uploadDir, directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        
        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = timestamp + "_" + UUID.randomUUID().toString() + "." + extension;
        
        // 파일 저장
        Path filePath = directoryPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 접근 가능한 URL 반환
        String fileUrl = "/images/" + directory + "/" + uniqueFilename;
        
        log.info("File stored successfully: {}", fileUrl);
        return fileUrl;
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // URL에서 실제 파일 경로 추출
            String relativePath = fileUrl.replace("/images/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
                return true;
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String relativePath = fileUrl.replace("/images/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("Error checking file existence: {}", fileUrl, e);
            return false;
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ProductResultCode.IMAGE_SIZE_EXCEEDED);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}