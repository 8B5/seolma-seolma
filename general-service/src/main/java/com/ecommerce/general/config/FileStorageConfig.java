package com.ecommerce.general.config;

import com.ecommerce.product.service.FileStorageService;
import com.ecommerce.product.service.LocalFileStorageService;
import com.ecommerce.product.service.S3FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class FileStorageConfig {
    
    @Value("${file.storage.type:local}")
    private String storageType;
    
    @Bean
    public FileStorageService fileStorageService(S3Client s3Client) {
        System.out.println("=== FileStorageConfig DEBUG ===");
        System.out.println("Storage Type: " + storageType);
        System.out.println("FILE_STORAGE_TYPE env: " + System.getenv("FILE_STORAGE_TYPE"));
        System.out.println("================================");
        
        if ("s3".equalsIgnoreCase(storageType)) {
            System.out.println("Using S3FileStorageService");
            return new S3FileStorageService(s3Client);
        } else {
            System.out.println("Using LocalFileStorageService");
            return new LocalFileStorageService();
        }
    }
}