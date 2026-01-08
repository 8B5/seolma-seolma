package com.ecommerce.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;
    
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;  // 소유자 ID (VARCHAR(50))
    
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Builder
    public Coupon(Long templateId, String userId) {
        this.templateId = templateId;
        this.userId = userId;
        this.isUsed = false;
        this.issuedAt = LocalDateTime.now();
    }
    
    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다");
        }
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
    
    public boolean canUse() {
        return !this.isUsed && this.usedAt == null;
    }
}