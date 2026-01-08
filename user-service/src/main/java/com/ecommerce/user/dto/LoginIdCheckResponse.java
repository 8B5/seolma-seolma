package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "로그인 아이디 중복확인 응답")
public class LoginIdCheckResponse {
    
    @Schema(description = "사용 가능 여부", example = "true")
    private final boolean available;
    
    @Schema(description = "확인한 로그인 아이디", example = "user123")
    private final String loginId;
    
    @Schema(description = "메시지", example = "사용 가능한 아이디입니다")
    private final String message;
    
    public static LoginIdCheckResponse available(String loginId) {
        return new LoginIdCheckResponse(true, loginId, "사용 가능한 아이디입니다");
    }
    
    public static LoginIdCheckResponse unavailable(String loginId) {
        return new LoginIdCheckResponse(false, loginId, "이미 사용 중인 아이디입니다");
    }
}