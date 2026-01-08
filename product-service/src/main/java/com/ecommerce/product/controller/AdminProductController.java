package com.ecommerce.product.controller;

import com.ecommerce.common.annotation.AdminOnly;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityUtils;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.ProductImageService;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "관리자 - 상품 관리", description = "관리자용 상품 관리 API")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@AdminOnly
public class AdminProductController {
    
    private final ProductService productService;
    private final ProductImageService productImageService;
    
    @Operation(summary = "상품 등록", description = "관리자가 새로운 상품을 등록합니다")
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        String sellerId = SecurityUtils.getCurrentUserId();
        ProductResponse response = productService.createProduct(request, sellerId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "전체 상품 목록 조회", description = "관리자가 모든 상품을 조회합니다 (삭제된 상품 포함)")
    @GetMapping
    public ApiResponse<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "판매자 ID") @RequestParam(required = false) String sellerId,
            @Parameter(description = "최소 가격") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "삭제된 상품 포함 여부") @RequestParam(defaultValue = "true") Boolean includeDeleted,
            @PageableDefault(size = 20) Pageable pageable) {
        
        ProductSearchCondition condition = new ProductSearchCondition(
                keyword, sellerId, minPrice, maxPrice);
        
        Page<ProductResponse> response = productService.searchProductsForAdmin(condition, includeDeleted, pageable);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 수정", description = "관리자가 상품 정보를 수정합니다")
    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        
        String sellerId = SecurityUtils.getCurrentUserId();
        ProductResponse response = productService.updateProductByAdmin(productId, request, sellerId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 삭제", description = "관리자가 상품을 삭제합니다 (소프트 삭제)")
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId) {
        
        String sellerId = SecurityUtils.getCurrentUserId();
        productService.deleteProduct(productId, sellerId);
        return ApiResponse.success();
    }

    @Operation(summary = "상품 이미지 업로드", description = "관리자가 상품에 이미지를 업로드합니다")
    @PostMapping(value = "/{productId}/images", consumes = "multipart/form-data")
    public ApiResponse<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        String sellerId = SecurityUtils.getCurrentUserId();
        ProductImageResponse response = productImageService.uploadImage(productId, file, sellerId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 이미지 목록 조회", description = "상품의 모든 이미지를 조회합니다")
    @GetMapping("/{productId}/images")
    public ApiResponse<List<ProductImageResponse>> getProductImages(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId) {
        
        List<ProductImageResponse> response = productImageService.getProductImages(productId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 이미지 삭제", description = "관리자가 상품의 특정 이미지를 삭제합니다")
    @DeleteMapping("/{productId}/images/{imageId}")
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Parameter(description = "이미지 ID", required = true) @PathVariable Long imageId) {
        
        String sellerId = SecurityUtils.getCurrentUserId();
        productImageService.deleteImage(productId, imageId, sellerId);
        return ApiResponse.success();
    }
}