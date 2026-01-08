package com.ecommerce.product.dto;

import com.ecommerce.product.domain.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class ProductResponse {
    
    private final Long id;
    private final String name;
    private final String sellerId;
    private final BigDecimal price;
    private final Boolean isDeleted;
    private final Boolean isAvailable;
    private final LocalDateTime createdAt;
    private final List<ProductImageResponse> images;
    
    public static ProductResponse from(Product product) {
        List<ProductImageResponse> imageResponses = product.getImages().stream()
                .filter(image -> !image.getIsDeleted())
                .map(ProductImageResponse::from)
                .collect(Collectors.toList());
        
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSellerId(),
                product.getPrice(),
                product.getIsDeleted(),
                product.isAvailable(),
                product.getCreatedAt(),
                imageResponses
        );
    }
}