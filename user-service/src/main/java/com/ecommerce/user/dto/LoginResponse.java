package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "로그인 응답")
public class LoginResponse {
    
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType = "Bearer";
    
    @Schema(description = "사용자 ID", example = "user123")
    private final String userId;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private final String userName;
    
    @Schema(description = "사용자 권한", example = "USER")
    private final String role;
}