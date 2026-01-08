package com.ecommerce.product.exception;

import com.ecommerce.common.response.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductResultCode implements ResultCode {
    PRODUCT_SUCCESS("P0000", "상품 처리 성공"),
    PRODUCT_CREATED("P0001", "상품이 등록되었습니다"),
    PRODUCT_UPDATED("P0002", "상품이 수정되었습니다"),
    PRODUCT_DELETED("P0003", "상품이 삭제되었습니다"),
    
    PRODUCT_NOT_FOUND("P1000", "상품을 찾을 수 없습니다"),
    PRODUCT_ALREADY_DELETED("P1001", "이미 삭제된 상품입니다"),
    UNAUTHORIZED_PRODUCT_ACCESS("P1005", "상품에 대한 권한이 없습니다"),
    
    IMAGE_UPLOAD_FAILED("P2000", "이미지 업로드에 실패했습니다"),
    IMAGE_NOT_FOUND("P2001", "이미지를 찾을 수 없습니다"),
    INVALID_IMAGE_FORMAT("P2002", "지원하지 않는 이미지 형식입니다"),
    IMAGE_SIZE_EXCEEDED("P2003", "이미지 크기가 너무 큽니다"),
    MAX_IMAGE_COUNT_EXCEEDED("P2004", "최대 이미지 개수를 초과했습니다"),
    PRODUCT_NOT_DELETED("P2005", "삭제되지 않은 상품입니다");

    private final String code;
    private final String message;
}