package com.ecommerce.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonResultCode implements ResultCode {
    SUCCESS("C0000", "성공"),
    INVALID_PARAMETER("C1000", "잘못된 파라미터입니다"),
    UNAUTHORIZED("C1001", "인증이 필요합니다"),
    FORBIDDEN("C1002", "권한이 없습니다"),
    NOT_FOUND("C1003", "리소스를 찾을 수 없습니다"),
    CONFLICT("C1004", "데이터 충돌이 발생했습니다"),
    SERVER_ERROR("C5000", "서버 내부 오류가 발생했습니다"),
    DATABASE_ERROR("C5001", "데이터베이스 오류가 발생했습니다"),
    EXTERNAL_API_ERROR("C5002", "외부 API 호출 중 오류가 발생했습니다");

    private final String code;
    private final String message;
}