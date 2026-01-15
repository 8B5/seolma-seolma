package com.ecommerce.user.dto;

import com.ecommerce.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {
    
    @NotBlank(message = "로그인 아이디는 필수입니다")
    @Size(min = 3, max = 50, message = "로그인 아이디는 3자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "로그인 아이디는 영문, 숫자, 언더스코어만 사용 가능합니다")
    @Schema(description = "로그인 아이디", example = "user123", required = true)
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[a-z\\d@$!%*?&]+$",
             message = "비밀번호는 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다")
    @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자/특수문자 포함)", example = "Password123!", required = true)
    private String password;
    
    @NotBlank(message = "사용자 이름은 필수입니다")
    @Size(min = 2, max = 50, message = "사용자 이름은 2자 이상 50자 이하여야 합니다")
    @Schema(description = "사용자 이름", example = "홍길동", required = true)
    private String userName;
    
    @Schema(description = "사용자 권한 (USER: 일반 사용자, ADMIN: 관리자)", 
            example = "USER", 
            allowableValues = {"USER", "ADMIN"},
            defaultValue = "USER")
    private UserRole role;
    
    public SignupRequest(String loginId, String password, String userName) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.role = UserRole.USER; // 기본값
    }
    
    public SignupRequest(String loginId, String password, String userName, UserRole role) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.role = role;
    }
}