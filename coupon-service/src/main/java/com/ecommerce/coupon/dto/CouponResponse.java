package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.domain.Coupon;
import com.ecommerce.coupon.domain.CouponTemplate;
import com.ecommerce.coupon.domain.DiscountType;
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
    
    // 쿠폰 템플릿 정보
    private final String title;
    private final DiscountType discountType;
    private final String discountTypeDisplayName;
    private final Integer discountValue;
    private final String discountDisplay;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getTemplateId(),
                coupon.getUserId(),
                coupon.getIsUsed(),
                coupon.getUsedAt(),
                coupon.getIssuedAt(),
                coupon.canUse(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    
    public static CouponResponse fromWithTemplate(Coupon coupon, CouponTemplate template) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getTemplateId(),
                coupon.getUserId(),
                coupon.getIsUsed(),
                coupon.getUsedAt(),
                coupon.getIssuedAt(),
                coupon.canUse(),
                template.getTitle(),
                template.getDiscountType(),
                template.getDiscountType().getDisplayName(),
                template.getDiscountValue(),
                template.getDiscountType().formatDiscount(template.getDiscountValue()),
                template.getStartedAt(),
                template.getFinishedAt()
        );
    }
}