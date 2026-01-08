package com.ecommerce.coupon.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.coupon.domain.CouponTemplate;
import com.ecommerce.coupon.dto.CouponResponse;
import com.ecommerce.coupon.dto.CouponTemplateCreateRequest;
import com.ecommerce.coupon.dto.CouponTemplateResponse;
import com.ecommerce.coupon.exception.CouponResultCode;
import com.ecommerce.coupon.repository.CouponRepository;
import com.ecommerce.coupon.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCouponService {
    
    private final CouponTemplateRepository couponTemplateRepository;
    private final CouponRepository couponRepository;
    
    @Transactional
    public CouponTemplateResponse createCouponTemplate(CouponTemplateCreateRequest request, String adminId) {
        CouponTemplate template = CouponTemplate.builder()
                .title(request.getTitle())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .startedAt(request.getStartedAt())
                .finishedAt(request.getFinishedAt())
                .isLimited(request.getIsLimited())
                .totalQuantity(request.getTotalQuantity())
                .build();
        
        CouponTemplate savedTemplate = couponTemplateRepository.save(template);
        
        log.info("Coupon template created: templateId={}, title={}, isLimited={}, totalQuantity={}, adminId={}", 
                savedTemplate.getId(), savedTemplate.getTitle(), savedTemplate.getIsLimited(), 
                savedTemplate.getTotalQuantity(), adminId);
        
        return CouponTemplateResponse.from(savedTemplate);
    }
    
    public List<CouponTemplateResponse> getAllCouponTemplates() {
        return couponTemplateRepository.findByIsDeletedFalse()
                .stream()
                .map(template -> {
                    Long issuedCount = couponRepository.countByTemplateId(template.getId());
                    return CouponTemplateResponse.fromWithIssuedCount(template, issuedCount);
                })
                .collect(Collectors.toList());
    }
    
    public CouponTemplateResponse getCouponTemplate(Long templateId) {
        CouponTemplate template = couponTemplateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new BusinessException(CouponResultCode.TEMPLATE_NOT_FOUND));
        
        Long issuedCount = couponRepository.countByTemplateId(templateId);
        return CouponTemplateResponse.fromWithIssuedCount(template, issuedCount);
    }
    
    @Transactional
    public CouponTemplateResponse updateCouponTemplate(Long templateId, CouponTemplateCreateRequest request, String adminId) {
        CouponTemplate template = couponTemplateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new BusinessException(CouponResultCode.TEMPLATE_NOT_FOUND));
        
        template.updateInfo(request.getTitle(), request.getDiscountType(), request.getDiscountValue(),
                          request.getStartedAt(), request.getFinishedAt(), 
                          request.getIsLimited(), request.getTotalQuantity());
        
        log.info("Coupon template updated: templateId={}, title={}, isLimited={}, totalQuantity={}, adminId={}", 
                templateId, request.getTitle(), request.getIsLimited(), request.getTotalQuantity(), adminId);
        
        return CouponTemplateResponse.from(template);
    }
    
    @Transactional
    public void deleteCouponTemplate(Long templateId, String adminId) {
        CouponTemplate template = couponTemplateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new BusinessException(CouponResultCode.TEMPLATE_NOT_FOUND));
        
        // 발급된 쿠폰이 있는지 확인
        Long issuedCount = couponRepository.countByTemplateId(templateId);
        if (issuedCount > 0) {
            throw new BusinessException(CouponResultCode.TEMPLATE_HAS_ISSUED_COUPONS);
        }
        
        // 소프트 삭제
        template.delete();
        
        log.info("Coupon template soft deleted: templateId={}, adminId={}", templateId, adminId);
    }
    
    public Page<CouponResponse> getAllIssuedCoupons(Long templateId, String userId, Boolean isUsed, Pageable pageable) {
        // 유효한 정렬 필드만 허용
        Pageable validatedPageable = validateAndFixPageable(pageable);
        
        return couponRepository.findAllWithConditions(templateId, userId, isUsed, validatedPageable)
                .map(CouponResponse::from);
    }
    
    private Pageable validateAndFixPageable(Pageable pageable) {
        // Coupon 엔티티의 유효한 필드명들
        List<String> validSortFields = List.of("id", "templateId", "userId", "isUsed", "usedAt", "issuedAt");
        
        if (pageable.getSort().isSorted()) {
            // 정렬 필드 검증
            boolean hasInvalidSort = pageable.getSort().stream()
                    .anyMatch(order -> !validSortFields.contains(order.getProperty()));
            
            if (hasInvalidSort) {
                // 유효하지 않은 정렬 필드가 있으면 기본 정렬(issuedAt DESC)로 변경
                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.DESC, "issuedAt"));
            }
        }
        
        return pageable;
    }
}