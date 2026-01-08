package com.ecommerce.user.exception;

import com.ecommerce.common.response.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginResultCode implements ResultCode {
    LOGIN_SUCCESS("U0000", "로그인 성공"),
    SIGNUP_SUCCESS("U0001", "회원가입 성공"),
    INVALID_CREDENTIALS("U1000", "아이디 또는 비밀번호가 올바르지 않습니다"),
    USER_NOT_FOUND("U1001", "존재하지 않는 사용자입니다"),
    DUPLICATE_LOGIN_ID("U1002", "이미 사용 중인 로그인 아이디입니다"),
    WEAK_PASSWORD("U1003", "비밀번호가 보안 정책에 맞지 않습니다"),
    ACCOUNT_LOCKED("U1004", "계정이 잠겨있습니다"),
    TOKEN_EXPIRED("U1005", "토큰이 만료되었습니다"),
    INVALID_TOKEN("U1006", "유효하지 않은 토큰입니다"),
    INVALID_ADMIN_SECRET("U1007", "관리자 등록 권한이 없습니다");

    private final String code;
    private final String message;
}