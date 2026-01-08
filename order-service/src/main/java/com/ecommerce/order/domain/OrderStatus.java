package com.ecommerce.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PAYMENT_COMPLETED("결제완료", "결제가 완료된 상태"),
    PREPARING("상품준비중", "상품을 준비하는 중"),
    SHIPPING("배송중", "상품이 배송 중인 상태"),
    DELIVERED("배송완료", "배송이 완료된 상태"),
    CANCELLED("주문취소", "주문이 취소된 상태");
    
    private final String displayName;
    private final String description;
}