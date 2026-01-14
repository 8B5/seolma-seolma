package com.ecommerce.product.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "상품 조회", description = "상품 조회 및 검색 API (읽기 전용)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId) {
        
        ProductResponse response = productService.getProduct(productId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 목록 조회", description = "판매 중인 상품 목록을 조회합니다")
    @GetMapping
    public ApiResponse<Page<ProductResponse>> getProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        
        Page<ProductResponse> response = productService.getProducts(keyword, pageable);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "판매자별 상품 조회", description = "특정 판매자의 상품 목록을 조회합니다")
    @GetMapping("/seller/{sellerId}")
    public ApiResponse<Page<ProductResponse>> getProductsBySeller(
            @Parameter(description = "판매자 ID", required = true) @PathVariable String sellerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ProductResponse> response = productService.getProductsBySeller(sellerId, pageable);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "상품 검색", description = "다양한 조건으로 상품을 검색합니다")
    @GetMapping("/search")
    public ApiResponse<Page<ProductResponse>> searchProducts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "판매자 ID") @RequestParam(required = false) String sellerId,
            @Parameter(description = "최소 가격") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        
        ProductSearchCondition condition = new ProductSearchCondition(
                keyword, sellerId, minPrice, maxPrice);
        
        Page<ProductResponse> response = productService.searchProducts(condition, pageable);
        return ApiResponse.success(response);
    }
}