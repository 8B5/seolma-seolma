package com.ecommerce.coupon.repository;

import com.ecommerce.coupon.domain.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    
    // 삭제되지 않은 모든 쿠폰 템플릿 조회
    List<CouponTemplate> findByIsDeletedFalse();
    
    // 삭제되지 않은 특정 쿠폰 템플릿 조회
    Optional<CouponTemplate> findByIdAndIsDeletedFalse(Long id);
    
    // 현재 발급 가능한 쿠폰 템플릿 조회 (삭제되지 않은 것만)
    @Query("SELECT ct FROM CouponTemplate ct WHERE ct.isDeleted = false AND ct.startedAt <= :now AND ct.finishedAt > :now")
    List<CouponTemplate> findAvailableTemplates(LocalDateTime now);
    
    // 만료되지 않은 쿠폰 템플릿 조회 (삭제되지 않은 것만)
    @Query("SELECT ct FROM CouponTemplate ct WHERE ct.isDeleted = false AND ct.finishedAt > :now ORDER BY ct.startedAt ASC")
    List<CouponTemplate> findActiveTemplates(LocalDateTime now);
}