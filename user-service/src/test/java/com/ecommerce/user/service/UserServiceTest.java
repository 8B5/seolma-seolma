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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        SignupRequest request = new SignupRequest("testuser", "Password123!", "테스트유저");
        given(userRepository.existsById(request.getLoginId())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        
        // when
        assertThatCode(() -> userService.signup(request))
                .doesNotThrowAnyException();
        
        // then
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("중복 아이디로 회원가입 실패")
    void signup_DuplicateLoginId() {
        // given
        SignupRequest request = new SignupRequest("testuser", "Password123!", "테스트유저");
        given(userRepository.existsById(request.getLoginId())).willReturn(true);
        
        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", LoginResultCode.DUPLICATE_LOGIN_ID);
    }
    
    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest("testuser", "Password123!");
        User user = User.builder()
                .loginId("testuser")
                .password("encodedPassword")
                .userName("테스트유저")
                .role(UserRole.USER)
                .build();
        
        given(userRepository.findById(request.getLoginId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(anyString(), anyString())).willReturn("jwt-token");
        
        // when
        LoginResponse response = userService.login(request);
        
        // then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUserName()).isEqualTo("테스트유저");
        assertThat(response.getRole()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 실패")
    void login_UserNotFound() {
        // given
        LoginRequest request = new LoginRequest("nonexistent", "Password123!");
        given(userRepository.findById(request.getLoginId())).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", LoginResultCode.INVALID_CREDENTIALS);
    }
}