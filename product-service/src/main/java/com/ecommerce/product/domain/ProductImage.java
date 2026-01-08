package com.ecommerce.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public ProductImage(Product product, String imageUrl) {
        this.product = product;
        this.imageUrl = imageUrl;
    }
    
    // 연관관계 편의 메서드
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public void delete() {
        this.isDeleted = true;
    }
    
    public void restore() {
        this.isDeleted = false;
    }
}