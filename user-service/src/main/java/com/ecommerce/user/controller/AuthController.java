package com.ecommerce.user.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.user.dto.LoginIdCheckResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.SignupRequest;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @Operation(
        summary = "회원가입", 
        description = "새로운 사용자를 등록합니다. 일반 사용자 또는 관리자로 등록할 수 있습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 정보",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupRequest.class),
                examples = {
                    @ExampleObject(
                        name = "일반 사용자 가입",
                        description = "일반 사용자로 회원가입",
                        value = """
                        {
                          "loginId": "user123",
                          "password": "Password123!",
                          "userName": "홍길동"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "관리자 가입",
                        description = "관리자로 회원가입 (환경변수 ADMIN_SECRET_KEY 설정 필요)",
                        value = """
                        {
                          "loginId": "admin123",
                          "password": "AdminPass123!",
                          "userName": "관리자",
                          "role": "ADMIN"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "C0000",
                      "message": "회원가입이 완료되었습니다",
                      "data": null,
                      "timestamp": "2025-01-08T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (검증 실패 또는 중복 아이디)",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "검증 실패",
                        value = """
                        {
                          "code": "C0001",
                          "message": "비밀번호는 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다",
                          "data": null,
                          "timestamp": "2025-01-08T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "중복 아이디",
                        value = """
                        {
                          "code": "L0001",
                          "message": "이미 존재하는 로그인 아이디입니다",
                          "data": null,
                          "timestamp": "2025-01-08T10:30:00"
                        }
                        """
                    )
                }
            )
        )
    })
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ApiResponse.success();
    }
    
    @Operation(
        summary = "로그인", 
        description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정보",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "loginId": "user123",
                      "password": "Password123!"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "C0000",
                      "message": "로그인 성공",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "userId": "user123",
                        "userName": "홍길동",
                        "role": "USER"
                      },
                      "timestamp": "2025-01-08T10:30:00"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "로그인 실패 (잘못된 아이디 또는 비밀번호)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "L0002",
                      "message": "로그인 정보가 올바르지 않습니다",
                      "data": null,
                      "timestamp": "2025-01-08T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }
    
    @Operation(
        summary = "로그인 아이디 중복확인", 
        description = "회원가입 시 로그인 아이디의 사용 가능 여부를 확인합니다"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "중복확인 완료",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "사용 가능한 아이디",
                        value = """
                        {
                          "code": "C0000",
                          "message": "성공",
                          "data": {
                            "available": true,
                            "loginId": "user123",
                            "message": "사용 가능한 아이디입니다"
                          },
                          "timestamp": "2025-01-08T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "이미 사용 중인 아이디",
                        value = """
                        {
                          "code": "C0000",
                          "message": "성공",
                          "data": {
                            "available": false,
                            "loginId": "admin",
                            "message": "이미 사용 중인 아이디입니다"
                          },
                          "timestamp": "2025-01-08T10:30:00"
                        }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (아이디 형식 오류)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": "C0001",
                      "message": "로그인 아이디는 3자 이상 50자 이하여야 합니다",
                      "data": null,
                      "timestamp": "2025-01-08T10:30:00"
                    }
                    """
                )
            )
        )
    })
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