package com.ecommerce.coupon.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.coupon.domain.Coupon;
import com.ecommerce.coupon.domain.CouponTemplate;
import com.ecommerce.coupon.dto.CouponIssueRequest;
import com.ecommerce.coupon.dto.CouponResponse;
import com.ecommerce.coupon.dto.CouponTemplateResponse;
import com.ecommerce.coupon.exception.CouponResultCode;
import com.ecommerce.coupon.repository.CouponRepository;
import com.ecommerce.coupon.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
    
    private final CouponTemplateRepository couponTemplateRepository;
    private final CouponRepository couponRepository;
    
    /**
     * 발급 가능한 쿠폰 템플릿 목록 조회
     */
    public List<CouponTemplateResponse> getAvailableCouponTemplates() {
        LocalDateTime now = LocalDateTime.now();
        return couponTemplateRepository.findAvailableTemplates(now)
                .stream()
                .map(template -> {
                    Long issuedCount = couponRepository.countByTemplateId(template.getId());
                    return CouponTemplateResponse.fromWithIssuedCount(template, issuedCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 모든 쿠폰 템플릿 조회 (관리자용)
     */
    public List<CouponTemplateResponse> getAllCouponTemplates() {
        LocalDateTime now = LocalDateTime.now();
        return couponTemplateRepository.findActiveTemplates(now)
                .stream()
                .map(template -> {
                    Long issuedCount = couponRepository.countByTemplateId(template.getId());
                    return CouponTemplateResponse.fromWithIssuedCount(template, issuedCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 쿠폰 발급
     */
    @Transactional
    public CouponResponse issueCoupon(CouponIssueRequest request, String userId) {
        // 쿠폰 템플릿 조회 (삭제되지 않은 것만)
        CouponTemplate template = couponTemplateRepository.findByIdAndIsDeletedFalse(request.getTemplateId())
                .orElseThrow(() -> new BusinessException(CouponResultCode.TEMPLATE_NOT_FOUND));
        
        // 발급 기간 확인
        if (!template.isValidPeriod()) {
            if (template.isExpired()) {
                throw new BusinessException(CouponResultCode.TEMPLATE_EXPIRED);
            } else {
                throw new BusinessException(CouponResultCode.TEMPLATE_NOT_AVAILABLE);
            }
        }
        
        // 중복 발급 확인
        if (couponRepository.existsByTemplateIdAndUserId(request.getTemplateId(), userId)) {
            throw new BusinessException(CouponResultCode.ALREADY_ISSUED);
        }
        
        // 선착순 수량 확인 (동시성 제어를 위해 synchronized 사용)
        synchronized (this) {
            Long currentIssuedCount = couponRepository.countByTemplateId(request.getTemplateId());
            
            if (!template.canIssue(currentIssuedCount)) {
                if (template.isSoldOut(currentIssuedCount)) {
                    throw new BusinessException(CouponResultCode.COUPON_SOLD_OUT);
                } else {
                    throw new BusinessException(CouponResultCode.TEMPLATE_NOT_AVAILABLE);
                }
            }
            
            // 쿠폰 발급
            Coupon coupon = Coupon.builder()
                    .templateId(request.getTemplateId())
                    .userId(userId)
                    .build();
            
            Coupon savedCoupon = couponRepository.save(coupon);
            
            log.info("Coupon issued: templateId={}, userId={}, couponId={}, currentCount={}/{}", 
                    request.getTemplateId(), userId, savedCoupon.getId(), 
                    currentIssuedCount + 1, template.getTotalQuantity());
            
            return CouponResponse.from(savedCoupon);
        }
    }
    
    /**
     * 사용자별 쿠폰 목록 조회
     */
    public List<CouponResponse> getUserCoupons(String userId) {
        return couponRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자별 사용 가능한 쿠폰 목록 조회
     */
    public List<CouponResponse> getUserAvailableCoupons(String userId) {
        return couponRepository.findByUserIdAndIsUsedFalseOrderByIssuedAtDesc(userId)
                .stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 쿠폰 사용
     */
    @Transactional
    public void useCoupon(Long couponId, String userId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new BusinessException(CouponResultCode.COUPON_NOT_FOUND));
        
        if (!coupon.canUse()) {
            throw new BusinessException(CouponResultCode.COUPON_ALREADY_USED);
        }
        
        coupon.use();
        
        log.info("Coupon used: couponId={}, userId={}", couponId, userId);
    }
    
    /**
     * 쿠폰 상세 조회
     */
    public CouponResponse getCoupon(Long couponId, String userId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new BusinessException(CouponResultCode.COUPON_NOT_FOUND));
        
        return CouponResponse.from(coupon);
    }
}