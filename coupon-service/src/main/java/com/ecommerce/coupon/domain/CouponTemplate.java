package com.ecommerce.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;
    
    @Column(name = "is_limited", nullable = false)
    private Boolean isLimited = false;  // 수량 제한 여부
    
    @Column(name = "total_quantity")
    private Integer totalQuantity;  // 총 발급 가능 수량 (isLimited가 true일 때만 사용)
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;  // 삭제 여부
    
    @Builder
    public CouponTemplate(String title, DiscountType discountType, Integer discountValue,
                         LocalDateTime startedAt, LocalDateTime finishedAt, 
                         Boolean isLimited, Integer totalQuantity) {
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.isLimited = isLimited != null ? isLimited : false;
        this.totalQuantity = totalQuantity;
        this.isDeleted = false;
    }
    
    public void updateInfo(String title, DiscountType discountType, Integer discountValue,
                          LocalDateTime startedAt, LocalDateTime finishedAt,
                          Boolean isLimited, Integer totalQuantity) {
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.isLimited = isLimited != null ? isLimited : false;
        this.totalQuantity = totalQuantity;
    }
    
    public boolean isValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startedAt) && now.isBefore(finishedAt);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(finishedAt);
    }
    
    public boolean canIssue(Long currentIssuedCount) {
        // 기간 체크
        if (!isValidPeriod()) {
            return false;
        }
        
        // 수량 제한이 있는 경우 수량 체크
        if (isLimited && totalQuantity != null) {
            return currentIssuedCount < totalQuantity;
        }
        
        return true;
    }
    
    public boolean isSoldOut(Long currentIssuedCount) {
        return isLimited && totalQuantity != null && currentIssuedCount >= totalQuantity;
    }
    
    public void delete() {
        this.isDeleted = true;
    }
    
    public boolean isActive() {
        return !this.isDeleted;
    }
}