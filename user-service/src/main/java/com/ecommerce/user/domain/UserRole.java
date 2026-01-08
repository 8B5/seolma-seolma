package com.ecommerce.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자 권한")
public enum UserRole {
    @Schema(description = "일반 사용자")
    USER("ROLE_USER", "일반 사용자"),
    
    @Schema(description = "관리자")
    ADMIN("ROLE_ADMIN", "관리자");
    
    private final String authority;
    private final String description;
}