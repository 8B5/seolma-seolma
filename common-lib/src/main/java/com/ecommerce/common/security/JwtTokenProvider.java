package com.ecommerce.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long tokenValidityInMilliseconds;
    
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds:3600}") long tokenValidityInSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }
    
    public String createToken(String subject, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }
    
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }
    
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            getClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}