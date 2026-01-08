package com.ecommerce.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "seller_id", nullable = false)
    private String sellerId;  // 등록한 관리자 ID (VARCHAR(50))
    
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();
    
    @Builder
    public Product(String name, String sellerId, BigDecimal price) {
        this.name = name;
        this.sellerId = sellerId;
        this.price = price;
    }
    
    // 비즈니스 메서드
    public void updateBasicInfo(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
    
    public void delete() {
        this.isDeleted = true;
    }
    
    public void restore() {
        this.isDeleted = false;
    }
    
    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
    }
    
    public boolean isAvailable() {
        return !isDeleted;
    }
}