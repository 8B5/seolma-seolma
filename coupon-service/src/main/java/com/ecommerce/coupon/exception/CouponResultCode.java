package com.ecommerce.coupon.exception;

import com.ecommerce.common.response.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponResultCode implements ResultCode {
    COUPON_SUCCESS("CP0000", "쿠폰 처리 성공"),
    COUPON_ISSUED("CP0001", "쿠폰이 발급되었습니다"),
    COUPON_USED("CP0002", "쿠폰이 사용되었습니다"),
    
    TEMPLATE_NOT_FOUND("CP1000", "쿠폰 템플릿을 찾을 수 없습니다"),
    COUPON_NOT_FOUND("CP1001", "쿠폰을 찾을 수 없습니다"),
    TEMPLATE_NOT_AVAILABLE("CP1002", "발급 기간이 아닌 쿠폰입니다"),
    TEMPLATE_EXPIRED("CP1003", "만료된 쿠폰 템플릿입니다"),
    ALREADY_ISSUED("CP1004", "이미 발급받은 쿠폰입니다"),
    COUPON_ALREADY_USED("CP1005", "이미 사용된 쿠폰입니다"),
    UNAUTHORIZED_COUPON_ACCESS("CP1006", "쿠폰에 대한 권한이 없습니다"),
    COUPON_NOT_USABLE("CP1007", "사용할 수 없는 쿠폰입니다"),
    TEMPLATE_HAS_ISSUED_COUPONS("CP1008", "발급된 쿠폰이 있어 삭제할 수 없습니다"),
    COUPON_SOLD_OUT("CP1009", "쿠폰이 모두 소진되었습니다");

    private final String code;
    private final String message;
}