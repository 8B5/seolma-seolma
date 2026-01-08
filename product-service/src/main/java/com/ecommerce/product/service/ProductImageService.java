package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductImage;
import com.ecommerce.product.dto.ProductImageResponse;
import com.ecommerce.product.exception.ProductResultCode;
import com.ecommerce.product.repository.ProductImageRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    
    public List<ProductImageResponse> getProductImages(Long productId) {
        return productImageRepository.findByProductIdAndIsDeletedFalse(productId)
                .stream()
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file, String sellerId) {
        // 상품 존재 및 권한 확인
        Product product = findProductById(productId);
        validateSellerAccess(product, sellerId);
        
        try {
            // 파일 저장 (products 디렉토리에 저장)
            String imageUrl = fileStorageService.storeFile(file, "products");
            
            // 이미지 엔티티 생성
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrl)
                    .build();
            
            ProductImage savedImage = productImageRepository.save(image);
            
            log.info("Image uploaded: productId={}, imageId={}, filename={}, sellerId={}", 
                    productId, savedImage.getId(), file.getOriginalFilename(), sellerId);
            
            return ProductImageResponse.from(savedImage);
            
        } catch (IOException e) {
            log.error("Failed to upload image: productId={}, filename={}", 
                    productId, file.getOriginalFilename(), e);
            throw new BusinessException(ProductResultCode.IMAGE_UPLOAD_FAILED);
        }
    }
    
    @Transactional
    public void deleteImage(Long productId, Long imageId, String sellerId) {
        // 상품 존재 및 권한 확인
        Product product = findProductById(productId);
        validateSellerAccess(product, sellerId);
        
        // 이미지 조회
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ProductResultCode.IMAGE_NOT_FOUND));
        
        // 이미지가 해당 상품의 것인지 확인
        if (!image.getProduct().getId().equals(productId)) {
            throw new BusinessException(ProductResultCode.IMAGE_NOT_FOUND);
        }
        
        // 파일 시스템에서 파일 삭제 (선택적)
        try {
            fileStorageService.deleteFile(image.getImageUrl());
        } catch (Exception e) {
            log.warn("Failed to delete physical file: {}", image.getImageUrl(), e);
            // 물리적 파일 삭제 실패해도 DB에서는 소프트 삭제 진행
        }
        
        // 소프트 삭제
        image.delete();
        
        log.info("Image deleted: productId={}, imageId={}, sellerId={}", 
                productId, imageId, sellerId);
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(ProductResultCode.PRODUCT_NOT_FOUND));
    }
    
    private void validateSellerAccess(Product product, String sellerId) {
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(ProductResultCode.UNAUTHORIZED_PRODUCT_ACCESS);
        }
    }
}