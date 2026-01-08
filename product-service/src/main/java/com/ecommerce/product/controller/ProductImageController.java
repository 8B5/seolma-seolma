package com.ecommerce.product.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityUtils;
import com.ecommerce.product.dto.ProductImageResponse;
import com.ecommerce.product.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "상품 이미지", description = "상품 이미지 관리 API")
@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {
    
    private final ProductImageService productImageService;
    
    @Operation(summary = "상품 이미지 목록 조회", description = "상품의 모든 이미지를 조회합니다")
    @GetMapping
    public ApiResponse<List<ProductImageResponse>> getProductImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId) {
        
        List<ProductImageResponse> response = productImageService.getProductImages(productId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 이미지 업로드", description = "상품에 새로운 이미지를 업로드합니다")
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        
        String sellerId = SecurityUtils.getCurrentUserId();
        ProductImageResponse response = productImageService.uploadImage(productId, file, sellerId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 이미지 삭제", description = "상품의 특정 이미지를 삭제합니다")
    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Parameter(description = "이미지 ID", required = true) @PathVariable Long imageId) {
        
        String sellerId = SecurityUtils.getCurrentUserId();
        productImageService.deleteImage(productId, imageId, sellerId);
        return ApiResponse.success();
    }
    

}