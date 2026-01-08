package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    
    @NotBlank(message = "로그인 아이디는 필수입니다")
    @Size(min = 3, max = 50, message = "로그인 아이디는 3자 이상 50자 이하여야 합니다")
    @Schema(description = "로그인 아이디", example = "user123", required = true)
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Schema(description = "비밀번호", example = "Password123!", required = true)
    private String password;
    
    public LoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
}