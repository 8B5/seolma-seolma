package com.ecommerce.product.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "내부 상품 API", description = "다른 마이크로서비스에서 호출하는 내부 API")
@RestController
@RequestMapping("/api/v1/internal/products")
@RequiredArgsConstructor
public class InternalProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "상품 정보 조회 (내부 API)", description = "다른 서비스에서 상품 정보를 조회합니다")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductForInternal(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId) {
        
        ProductResponse response = productService.getProduct(productId);
        return ApiResponse.success(response);
    }
}