package com.ecommerce.coupon.repository;

import com.ecommerce.coupon.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // 사용자별 쿠폰 조회 (사용 여부 상관없이)
    List<Coupon> findByUserIdOrderByIssuedAtDesc(String userId);
    
    // 사용자별 사용 가능한 쿠폰 조회
    List<Coupon> findByUserIdAndIsUsedFalseOrderByIssuedAtDesc(String userId);
    
    // 특정 템플릿으로 사용자가 이미 발급받았는지 확인
    boolean existsByTemplateIdAndUserId(Long templateId, String userId);
    
    // 사용자의 특정 쿠폰 조회
    Optional<Coupon> findByIdAndUserId(Long couponId, String userId);
    
    // 템플릿별 발급 수량 조회
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId")
    Long countByTemplateId(@Param("templateId") Long templateId);
    
    // 템플릿별 사용된 쿠폰 수량 조회
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.templateId = :templateId AND c.isUsed = true")
    Long countByTemplateIdAndIsUsedTrue(@Param("templateId") Long templateId);
    
    // 관리자용 쿠폰 검색
    @Query("SELECT c FROM Coupon c WHERE " +
           "(:templateId IS NULL OR c.templateId = :templateId) AND " +
           "(:userId IS NULL OR c.userId = :userId) AND " +
           "(:isUsed IS NULL OR c.isUsed = :isUsed)")
    Page<Coupon> findAllWithConditions(@Param("templateId") Long templateId, 
                                      @Param("userId") String userId, 
                                      @Param("isUsed") Boolean isUsed, 
                                      Pageable pageable);
}