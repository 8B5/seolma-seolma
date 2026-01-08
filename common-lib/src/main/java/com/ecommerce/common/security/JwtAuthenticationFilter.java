package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = resolveToken(request);
        
        if (StringUtils.hasText(token)) {
            if (jwtTokenProvider.validateToken(token)) {
                // 유효한 토큰인 경우 인증 정보 설정
                String userId = jwtTokenProvider.getSubject(token);
                String role = jwtTokenProvider.getRole(token);
                
                // SecurityContext에 인증 정보 설정
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        List.of(new SimpleGrantedAuthority(role))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 헤더에 사용자 정보 추가 (다른 서비스에서 사용할 수 있도록)
                request.setAttribute("X-User-Id", userId);
                request.setAttribute("X-User-Role", role);
            } else if (jwtTokenProvider.isTokenExpired(token) && isAuthenticationRequired(request)) {
                // 만료된 토큰이고 인증이 필요한 경로인 경우 401 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    "{\"code\":\"C0002\",\"message\":\"JWT 토큰이 만료되었습니다\",\"data\":null,\"timestamp\":\"" + 
                    java.time.LocalDateTime.now() + "\"}"
                );
                return;
            } else if (!jwtTokenProvider.validateToken(token) && isAuthenticationRequired(request)) {
                // 잘못된 토큰이고 인증이 필요한 경로인 경우 401 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    "{\"code\":\"C0002\",\"message\":\"JWT 토큰이 유효하지 않습니다\",\"data\":null,\"timestamp\":\"" + 
                    java.time.LocalDateTime.now() + "\"}"
                );
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isAuthenticationRequired(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요한 경로들
        return path.startsWith("/api/v1/admin/") || 
               path.startsWith("/api/v1/orders/") || 
               path.startsWith("/api/v1/coupons/") && !path.equals("/api/v1/coupons/templates/available");
    }
}