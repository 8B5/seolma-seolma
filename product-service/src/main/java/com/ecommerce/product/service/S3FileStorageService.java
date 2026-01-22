package com.ecommerce.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * AWS S3를 사용한 파일 저장 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.s3.cloudfront-domain:}")
    private String cloudfrontDomain;
    
    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        System.out.println("=== S3FileStorageService.storeFile called ===");
        System.out.println("Bucket: " + bucketName);
        System.out.println("Region: " + region);
        System.out.println("Directory: " + directory);
        
        try {
            String key = directory + "/" + generateUniqueFileName(file);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
            
            // CloudFront 도메인이 설정되어 있으면 CloudFront URL 반환, 아니면 S3 URL 반환
            String resultUrl;
            if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
                resultUrl = String.format("https://%s/%s", cloudfrontDomain, key);
            } else {
                resultUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                        bucketName, region, key);
            }
            
            System.out.println("S3 Upload Success - URL: " + resultUrl);
            return resultUrl;
        } catch (Exception e) {
            System.out.println("S3 Upload Failed: " + e.getMessage());
            log.error("Failed to upload file to S3: {}", file.getOriginalFilename(), e);
            throw new IOException("S3 파일 업로드 실패", e);
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 object: {}", key);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete S3 object from URL: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking S3 object existence: {}", fileUrl, e);
            return false;
        }
    }
    
    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString() + "." + extension;
    }
    
    private String extractKeyFromUrl(String fileUrl) {
        // CloudFront URL인 경우
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty() && fileUrl.contains(cloudfrontDomain)) {
            return fileUrl.substring(fileUrl.indexOf(cloudfrontDomain) + cloudfrontDomain.length() + 1);
        }
        // S3 URL인 경우
        else if (fileUrl.contains(".s3.") && fileUrl.contains(".amazonaws.com/")) {
            return fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + 15);
        }
        // 기본적으로 마지막 / 이후를 key로 사용
        else {
            int lastSlashIndex = fileUrl.lastIndexOf('/');
            return lastSlashIndex != -1 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}