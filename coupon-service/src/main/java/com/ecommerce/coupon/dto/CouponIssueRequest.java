package com.ecommerce.coupon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouponIssueRequest {
    
    @NotNull(message = "쿠폰 템플릿 ID는 필수입니다")
    private Long templateId;
    
    public CouponIssueRequest(Long templateId) {
        this.templateId = templateId;
    }
}