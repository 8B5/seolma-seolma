package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.domain.Coupon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CouponResponse {
    
    private final Long id;
    private final Long templateId;
    private final String userId;
    private final Boolean isUsed;
    private final LocalDateTime usedAt;
    private final LocalDateTime issuedAt;
    private final Boolean canUse;
    
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getTemplateId(),
                coupon.getUserId(),
                coupon.getIsUsed(),
                coupon.getUsedAt(),
                coupon.getIssuedAt(),
                coupon.canUse()
        );
    }
}