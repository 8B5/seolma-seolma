package com.ecommerce.product.dto;

import com.ecommerce.product.domain.ProductImage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ProductImageResponse {
    
    private final Long id;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    
    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getCreatedAt()
        );
    }
}