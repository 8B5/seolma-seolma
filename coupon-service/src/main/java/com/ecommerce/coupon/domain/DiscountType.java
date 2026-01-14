package com.ecommerce.coupon.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    PERCENT("퍼센트 할인", "%"),
    FIXED_AMOUNT("정액 할인", "원");
    
    private final String displayName;
    private final String unit;
    
    public String formatDiscount(Integer value) {
        if (this == PERCENT) {
            return value + "%";
        } else {
            return value + "원";
        }
    }
}