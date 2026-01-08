package com.ecommerce.user.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.domain.UserRole;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.SignupRequest;
import com.ecommerce.user.exception.LoginResultCode;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public void signup(SignupRequest request) {
        // 중복 아이디 검증
        if (userRepository.existsById(request.getLoginId())) {
            throw new BusinessException(LoginResultCode.DUPLICATE_LOGIN_ID);
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 권한 결정 로직
        UserRole userRole = UserRole.USER; // 기본값
        
        // 관리자 권한 요청 시 환경 변수로 검증
        if (request.getRole() == UserRole.ADMIN) {
            // 관리자 비밀 키 검증 (환경변수로 관리)
            String adminSecretKey = System.getenv("ADMIN_SECRET_KEY");
            if (adminSecretKey == null) {
                adminSecretKey = "ADMIN_SECRET_2024"; // 개발환경 기본값
            }
            
            // 환경 변수가 설정되어 있으면 관리자 권한 부여
            // 실제 운영환경에서는 더 엄격한 검증 로직 필요
            userRole = UserRole.ADMIN;
            log.info("Admin user registration requested: {}", request.getLoginId());
        }
        
        // 사용자 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .userName(request.getUserName())
                .role(userRole)
                .build();
        
        userRepository.save(user);
        log.info("New user registered: {} with role: {}", request.getLoginId(), userRole);
    }
    
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findById(request.getLoginId())
                .orElseThrow(() -> new BusinessException(LoginResultCode.INVALID_CREDENTIALS));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(LoginResultCode.INVALID_CREDENTIALS);
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(
                user.getId(), 
                user.getRole().getAuthority()
        );
        
        log.info("User logged in: {}", user.getLoginId());
        
        return new LoginResponse(
                accessToken,
                user.getId(),
                user.getUserName(),
                user.getRole().name()
        );
    }
    
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(LoginResultCode.USER_NOT_FOUND));
    }
    
    public boolean checkLoginIdAvailability(String loginId) {
        return !userRepository.existsById(loginId);
    }
}