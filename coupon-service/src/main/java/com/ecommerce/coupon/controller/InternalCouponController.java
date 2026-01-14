package com.ecommerce.coupon.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.coupon.dto.CouponResponse;
import com.ecommerce.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden  // Swagger에서 숨김
@RestController
@RequestMapping("/internal/v1/coupons")
@RequiredArgsConstructor
public class InternalCouponController {
    
    private final CouponService couponService;
    
    /**
     * 내부 서비스 간 통신용 - 쿠폰 사용 API
     * Order Service에서 호출
     */
    @PatchMapping("/{couponId}/use")
    public ApiResponse<Void> useCoupon(
            @PathVariable Long couponId,
            @RequestHeader("X-User-Id") String userId) {
        
        couponService.useCoupon(couponId, userId);
        return ApiResponse.success();
    }
    
    /**
     * 내부 서비스 간 통신용 - 쿠폰 정보 조회 API
     * Order Service에서 호출
     */
    @GetMapping("/{couponId}")
    public ApiResponse<CouponResponse> getCoupon(
            @PathVariable Long couponId,
            @RequestHeader("X-User-Id") String userId) {
        
        CouponResponse response = couponService.getCoupon(couponId, userId);
        return ApiResponse.success(response);
    }
}
