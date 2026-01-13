package com.ecommerce.user.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.user.dto.LoginIdCheckResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.SignupRequest;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @Operation(
        summary = "회원가입", 
        description = "새로운 사용자를 등록합니다. 일반 사용자 또는 관리자로 등록할 수 있습니다."
    )
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ApiResponse.success();
    }
    
    @Operation(
        summary = "로그인", 
        description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다"
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }
    
    @Operation(
        summary = "로그인 아이디 중복확인", 
        description = "회원가입 시 로그인 아이디의 사용 가능 여부를 확인합니다"
    )
    @GetMapping("/check-loginid")
    public ApiResponse<LoginIdCheckResponse> checkLoginId(
            @RequestParam("loginId") 
            @Schema(description = "확인할 로그인 아이디", example = "user123") 
            String loginId) {
        
        // 기본적인 형식 검증
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 아이디는 필수입니다");
        }
        
        if (loginId.length() < 3 || loginId.length() > 50) {
            throw new IllegalArgumentException("로그인 아이디는 3자 이상 50자 이하여야 합니다");
        }
        
        if (!loginId.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("로그인 아이디는 영문, 숫자, 언더스코어만 사용 가능합니다");
        }
        
        boolean available = userService.checkLoginIdAvailability(loginId);
        LoginIdCheckResponse response = available 
            ? LoginIdCheckResponse.available(loginId)
            : LoginIdCheckResponse.unavailable(loginId);
            
        return ApiResponse.success(response);
    }
}