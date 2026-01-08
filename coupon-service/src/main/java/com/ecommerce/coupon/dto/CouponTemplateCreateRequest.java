package com.ecommerce.coupon.dto;

import com.ecommerce.coupon.domain.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "쿠폰 템플릿 생성/수정 요청")
public class CouponTemplateCreateRequest {
    
    @Schema(description = "쿠폰명", example = "신규 회원 할인 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "쿠폰명은 필수입니다")
    @Size(max = 100, message = "쿠폰명은 100자 이하여야 합니다")
    private String title;
    
    @Schema(description = "할인 타입", example = "FIXED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "할인 타입은 필수입니다")
    private DiscountType discountType;
    
    @Schema(description = "할인 값 (정액할인: 원 단위, 정률할인: 퍼센트)", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "할인 값은 필수입니다")
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다")
    private Integer discountValue;
    
    @Schema(description = "사용 시작 일시", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "사용 시작 일시는 필수입니다")
    @Future(message = "사용 시작 일시는 현재 시간 이후여야 합니다")
    private LocalDateTime startedAt;
    
    @Schema(description = "사용 만료 일시", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "사용 만료 일시는 필수입니다")
    @Future(message = "사용 만료 일시는 현재 시간 이후여야 합니다")
    private LocalDateTime finishedAt;
    
    @Schema(description = "수량 제한 여부", example = "true")
    private Boolean isLimited = false;  // 수량 제한 여부 (기본값: false)
    
    @Schema(description = "총 발급 가능 수량 (수량 제한이 있는 경우 필수)", example = "100")
    @Min(value = 1, message = "총 발급 수량은 1 이상이어야 합니다")
    private Integer totalQuantity;  // 총 발급 가능 수량
    
    public CouponTemplateCreateRequest(String title, DiscountType discountType, Integer discountValue,
                                     LocalDateTime startedAt, LocalDateTime finishedAt,
                                     Boolean isLimited, Integer totalQuantity) {
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.isLimited = isLimited;
        this.totalQuantity = totalQuantity;
    }
    
    @AssertTrue(message = "사용 만료 일시는 시작 일시보다 늦어야 합니다")
    public boolean isValidPeriod() {
        if (startedAt == null || finishedAt == null) {
            return true; // 다른 validation에서 처리
        }
        return finishedAt.isAfter(startedAt);
    }
    
    @AssertTrue(message = "퍼센트 할인은 1~100 사이여야 합니다")
    public boolean isValidDiscountValue() {
        if (discountType == null || discountValue == null) {
            return true; // 다른 validation에서 처리
        }
        
        if (discountType == DiscountType.PERCENT) {
            return discountValue >= 1 && discountValue <= 100;
        }
        
        return discountValue >= 1;
    }
    
    @AssertTrue(message = "수량 제한이 있는 경우 총 발급 수량은 필수입니다")
    public boolean isValidQuantity() {
        if (isLimited == null || !isLimited) {
            return true; // 수량 제한이 없으면 검증 통과
        }
        return totalQuantity != null && totalQuantity > 0;
    }
}