package com.ecommerce.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("신용카드", "신용카드 결제"),
    TRANS("계좌이체", "계좌이체 결제"),
    VIRTUAL_ACCOUNT("가상계좌", "가상계좌 결제"),
    PHONE("휴대폰", "휴대폰 결제");
    
    private final String displayName;
    private final String description;
}