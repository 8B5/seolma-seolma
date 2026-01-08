package com.ecommerce.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @Column(name = "user_id", length = 50)
    private String id;  // PK이면서 로그인 아이디 역할
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public User(String loginId, String password, String userName, UserRole role) {
        this.id = loginId;  // user_id가 PK이면서 로그인 아이디
        this.password = password;
        this.userName = userName;
        this.role = role != null ? role : UserRole.USER;
    }
    
    // 로그인 아이디 getter (id와 동일)
    public String getLoginId() {
        return this.id;
    }
    
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
    
    public void updateUserName(String newUserName) {
        this.userName = newUserName;
    }
}