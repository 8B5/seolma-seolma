package com.ecommerce.user.config;

import com.ecommerce.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class UserSecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Internal API endpoints (서비스 간 통신용 - 인증 불필요)
                .requestMatchers("/api/v1/internal/**", "/api/internal/**", "/internal/**").permitAll()
                // Static resources (이미지 등)
                .requestMatchers("/images/**").permitAll()
                // User Service endpoints
                .requestMatchers("/api/v1/auth/**", "/api/v1/users/auth/**", "/users/auth/**").permitAll()
                .requestMatchers("/api/v1/users/me").authenticated()
                // Product Service endpoints
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/product-images/**").permitAll()
                // Order Service endpoints (require authentication)
                .requestMatchers("/api/v1/orders/**").authenticated()
                // Admin endpoints (require authentication)
                .requestMatchers("/api/v1/admin/**").authenticated()
                // System endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}