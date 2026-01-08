package com.ecommerce.coupon.controller;

import com.ecommerce.common.annotation.AdminOnly;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityUtils;
import com.ecommerce.coupon.dto.CouponResponse;
import com.ecommerce.coupon.dto.CouponTemplateCreateRequest;
import com.ecommerce.coupon.dto.CouponTemplateResponse;
import com.ecommerce.coupon.service.AdminCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 쿠폰 관리", description = "관리자용 쿠폰 관리 API")
@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@AdminOnly
public class AdminCouponController {
    
    private final AdminCouponService adminCouponService;
    
    @Operation(summary = "쿠폰 템플릿 등록", description = "관리자가 새로운 쿠폰 템플릿을 등록합니다")
    @PostMapping("/templates")
    public ApiResponse<CouponTemplateResponse> createCouponTemplate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "쿠폰 템플릿 생성 정보 (title, discountType, discountValue, startedAt, finishedAt 필수)",
                required = true
            )
            @Valid @RequestBody CouponTemplateCreateRequest request) {
        
        String adminId = SecurityUtils.getCurrentUserId();
        CouponTemplateResponse response = adminCouponService.createCouponTemplate(request, adminId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "모든 쿠폰 템플릿 조회", description = "관리자가 모든 쿠폰 템플릿을 조회합니다")
    @GetMapping("/templates")
    public ApiResponse<List<CouponTemplateResponse>> getAllCouponTemplates() {
        List<CouponTemplateResponse> response = adminCouponService.getAllCouponTemplates();
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 템플릿 상세 조회", description = "특정 쿠폰 템플릿의 상세 정보를 조회합니다")
    @GetMapping("/templates/{templateId}")
    public ApiResponse<CouponTemplateResponse> getCouponTemplate(
            @Parameter(description = "쿠폰 템플릿 ID", required = true) @PathVariable Long templateId) {
        
        CouponTemplateResponse response = adminCouponService.getCouponTemplate(templateId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 템플릿 수정", description = "관리자가 쿠폰 템플릿을 수정합니다")
    @PutMapping("/templates/{templateId}")
    public ApiResponse<CouponTemplateResponse> updateCouponTemplate(
            @Parameter(description = "쿠폰 템플릿 ID", required = true) @PathVariable Long templateId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "쿠폰 템플릿 수정 정보 (모든 필드 필수)",
                required = true
            )
            @Valid @RequestBody CouponTemplateCreateRequest request) {
        
        String adminId = SecurityUtils.getCurrentUserId();
        CouponTemplateResponse response = adminCouponService.updateCouponTemplate(templateId, request, adminId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "쿠폰 템플릿 삭제", description = "관리자가 쿠폰 템플릿을 삭제합니다")
    @DeleteMapping("/templates/{templateId}")
    public ApiResponse<Void> deleteCouponTemplate(
            @Parameter(description = "쿠폰 템플릿 ID", required = true) @PathVariable Long templateId) {
        
        String adminId = SecurityUtils.getCurrentUserId();
        adminCouponService.deleteCouponTemplate(templateId, adminId);
        return ApiResponse.success();
    }
    
    @Operation(summary = "전체 쿠폰 발급 내역 조회", description = "관리자가 모든 쿠폰 발급 내역을 조회합니다")
    @GetMapping("/issued")
    public ApiResponse<Page<CouponResponse>> getAllIssuedCoupons(
            @Parameter(description = "쿠폰 템플릿 ID") @RequestParam(required = false) Long templateId,
            @Parameter(description = "사용자 ID") @RequestParam(required = false) String userId,
            @Parameter(description = "사용 여부") @RequestParam(required = false) Boolean isUsed,
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<CouponResponse> response = adminCouponService.getAllIssuedCoupons(templateId, userId, isUsed, pageable);
        return ApiResponse.success(response);
    }
}