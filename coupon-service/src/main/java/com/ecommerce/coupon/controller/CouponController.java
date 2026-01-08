package com.ecommerce.coupon.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityUtils;
import com.ecommerce.coupon.dto.CouponIssueRequest;
import com.ecommerce.coupon.dto.CouponResponse;
import com.ecommerce.coupon.dto.CouponTemplateResponse;
import com.ecommerce.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "쿠폰", description = "쿠폰 발급 및 관리 API")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    @Operation(summary = "발급 가능한 쿠폰 목록 조회", description = "현재 발급 가능한 쿠폰 템플릿 목록을 조회합니다")
    @GetMapping("/templates/available")
    public ApiResponse<List<CouponTemplateResponse>> getAvailableCouponTemplates() {
        List<CouponTemplateResponse> response = couponService.getAvailableCouponTemplates();
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "모든 쿠폰 템플릿 조회", description = "모든 쿠폰 템플릿을 조회합니다 (관리자용)")
    @GetMapping("/templates")
    public ApiResponse<List<CouponTemplateResponse>> getAllCouponTemplates() {
        List<CouponTemplateResponse> response = couponService.getAllCouponTemplates();
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다")
    @PostMapping("/issue")
    public ApiResponse<CouponResponse> issueCoupon(@Valid @RequestBody CouponIssueRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        CouponResponse response = couponService.issueCoupon(request, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "내 쿠폰 목록 조회", description = "사용자의 모든 쿠폰 목록을 조회합니다")
    @GetMapping("/my")
    public ApiResponse<List<CouponResponse>> getMyCoupons() {
        String userId = SecurityUtils.getCurrentUserId();
        List<CouponResponse> response = couponService.getUserCoupons(userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "사용 가능한 내 쿠폰 조회", description = "사용자의 사용 가능한 쿠폰 목록을 조회합니다")
    @GetMapping("/my/available")
    public ApiResponse<List<CouponResponse>> getMyAvailableCoupons() {
        String userId = SecurityUtils.getCurrentUserId();
        List<CouponResponse> response = couponService.getUserAvailableCoupons(userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 상세 조회", description = "특정 쿠폰의 상세 정보를 조회합니다")
    @GetMapping("/{couponId}")
    public ApiResponse<CouponResponse> getCoupon(
            @Parameter(description = "쿠폰 ID", required = true) @PathVariable Long couponId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        CouponResponse response = couponService.getCoupon(couponId, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 사용", description = "쿠폰을 사용 처리합니다")
    @PatchMapping("/{couponId}/use")
    public ApiResponse<Void> useCoupon(
            @Parameter(description = "쿠폰 ID", required = true) @PathVariable Long couponId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        couponService.useCoupon(couponId, userId);
        return ApiResponse.success();
    }
}