package com.ecommerce.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 저장 서비스 인터페이스
 * 로컬 파일 시스템 또는 AWS S3 등 다양한 저장소를 지원하기 위한 추상화
 */
public interface FileStorageService {
    
    /**
     * 파일을 저장하고 접근 가능한 URL을 반환
     * 
     * @param file 저장할 파일
     * @param directory 저장할 디렉토리 (예: "products", "users")
     * @return 파일 접근 URL
     * @throws IOException 파일 저장 실패 시
     */
    String storeFile(MultipartFile file, String directory) throws IOException;
    
    /**
     * 파일을 삭제
     * 
     * @param fileUrl 삭제할 파일의 URL
     * @return 삭제 성공 여부
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 파일 존재 여부 확인
     * 
     * @param fileUrl 확인할 파일의 URL
     * @return 파일 존재 여부
     */
    boolean fileExists(String fileUrl);
}