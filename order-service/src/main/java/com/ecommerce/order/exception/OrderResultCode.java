package com.ecommerce.order.exception;

import com.ecommerce.common.response.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderResultCode implements ResultCode {
    ORDER_SUCCESS("O0000", "주문 처리 성공"),
    ORDER_CREATED("O0001", "주문이 생성되었습니다"),
    ORDER_CANCELLED("O0002", "주문이 취소되었습니다"),
    ORDER_STATUS_CHANGED("O0003", "주문 상태가 변경되었습니다"),
    
    ORDER_NOT_FOUND("O1000", "주문을 찾을 수 없습니다"),
    ORDER_CANNOT_CANCEL("O1001", "취소할 수 없는 주문입니다"),
    PRODUCT_NOT_FOUND("O1002", "상품을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK("O1003", "상품 재고가 부족합니다"),
    COUPON_USE_FAILED("O1004", "쿠폰 사용에 실패했습니다"),
    INVALID_ORDER_QUANTITY("O1005", "유효하지 않은 주문 수량입니다"),
    INVALID_PRODUCT_ID("O1006", "유효하지 않은 상품 ID입니다"),
    INVALID_QUANTITY("O1007", "유효하지 않은 주문 수량입니다"),
    INVALID_COUPON_ID("O1008", "유효하지 않은 쿠폰 ID입니다"),
    PAYMENT_FAILED("O1009", "결제 처리에 실패했습니다");

    private final String code;
    private final String message;
}