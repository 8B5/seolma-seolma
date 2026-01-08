package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.domain.CouponTemplate;
import com.ecommerce.coupon.domain.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Schema(description = "쿠폰 템플릿 응답")
public class CouponTemplateResponse {
    
    @Schema(description = "쿠폰 템플릿 ID", example = "1")
    private final Long id;
    
    @Schema(description = "쿠폰명", example = "신규 회원 할인 쿠폰")
    private final String title;
    
    @Schema(description = "할인 타입")
    private final DiscountType discountType;
    
    @Schema(description = "할인 타입 표시명", example = "정액할인")
    private final String discountTypeDisplayName;
    
    @Schema(description = "할인 값", example = "5000")
    private final Integer discountValue;
    
    @Schema(description = "할인 표시", example = "5000원")
    private final String discountDisplay;
    
    @Schema(description = "사용 시작 일시")
    private final LocalDateTime startedAt;
    
    @Schema(description = "사용 만료 일시")
    private final LocalDateTime finishedAt;
    
    @Schema(description = "수량 제한 여부", example = "true")
    private final Boolean isLimited;
    
    @Schema(description = "총 발급 가능 수량", example = "100")
    private final Integer totalQuantity;
    
    @Schema(description = "현재 발급된 수량", example = "75")
    private final Long issuedCount;
    
    @Schema(description = "매진 여부", example = "false")
    private final Boolean isSoldOut;
    
    @Schema(description = "현재 발급 가능 여부", example = "true")
    private final Boolean isAvailable;
    
    @Schema(description = "만료 여부", example = "false")
    private final Boolean isExpired;
    
    @Schema(description = "삭제 여부", example = "false")
    private final Boolean isDeleted;
    
    public static CouponTemplateResponse from(CouponTemplate template) {
        String discountDisplay = template.getDiscountType() == DiscountType.PERCENT 
            ? template.getDiscountValue() + "%" 
            : template.getDiscountValue() + "원";
            
        return new CouponTemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getDiscountType(),
                template.getDiscountType().getDisplayName(),
                template.getDiscountValue(),
                discountDisplay,
                template.getStartedAt(),
                template.getFinishedAt(),
                template.getIsLimited(),
                template.getTotalQuantity(),
                null,  // issuedCount는 별도 조회 필요
                null,  // isSoldOut은 별도 계산 필요
                template.isValidPeriod(),
                template.isExpired(),
                template.getIsDeleted()
        );
    }
    
    public static CouponTemplateResponse fromWithIssuedCount(CouponTemplate template, Long issuedCount) {
        String discountDisplay = template.getDiscountType() == DiscountType.PERCENT 
            ? template.getDiscountValue() + "%" 
            : template.getDiscountValue() + "원";
        
        boolean isSoldOut = template.isSoldOut(issuedCount);
        boolean isAvailable = template.canIssue(issuedCount);
            
        return new CouponTemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getDiscountType(),
                template.getDiscountType().getDisplayName(),
                template.getDiscountValue(),
                discountDisplay,
                template.getStartedAt(),
                template.getFinishedAt(),
                template.getIsLimited(),
                template.getTotalQuantity(),
                issuedCount,
                isSoldOut,
                isAvailable,
                template.isExpired(),
                template.getIsDeleted()
        );
    }
}