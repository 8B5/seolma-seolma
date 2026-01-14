package com.ecommerce.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${external.services.coupon-service.url}")
    private String couponServiceUrl;
    
    public void useCoupon(Long couponId, String userId) {
        try {
            webClientBuilder.build()
                    .patch()
                    .uri(couponServiceUrl + "/internal/v1/coupons/{couponId}/use", couponId)
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to use coupon: couponId={}, userId={}", couponId, userId, e);
            throw new RuntimeException("쿠폰 사용 실패", e);
        }
    }
    
    public CouponInfo getCouponInfo(Long couponId, String userId) {
        try {
            log.debug("Requesting coupon info: couponId={}, userId={}, url={}", couponId, userId, couponServiceUrl);
            
            com.ecommerce.common.response.ApiResponse<CouponResponse> response = webClientBuilder.build()
                    .get()
                    .uri(couponServiceUrl + "/internal/v1/coupons/{couponId}", couponId)
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<com.ecommerce.common.response.ApiResponse<CouponResponse>>() {})
                    .block();
            
            if (response == null || response.getData() == null) {
                log.error("Empty response from coupon service: couponId={}", couponId);
                throw new RuntimeException("쿠폰 정보를 찾을 수 없습니다");
            }
            
            CouponResponse couponData = response.getData();
            log.debug("Successfully retrieved coupon info: couponId={}, discountType={}, discountValue={}", 
                    couponId, couponData.getDiscountType(), couponData.getDiscountValue());
            
            return new CouponInfo(couponData.getDiscountType(), couponData.getDiscountValue());
        } catch (Exception e) {
            log.error("Failed to get coupon info: couponId={}, userId={}", couponId, userId, e);
            throw new RuntimeException("쿠폰 정보 조회 실패", e);
        }
    }
    
    // Inner class for coupon response from coupon service
    public static class CouponResponse {
        private String discountType;
        private Integer discountValue;
        
        public String getDiscountType() {
            return discountType;
        }
        
        public void setDiscountType(String discountType) {
            this.discountType = discountType;
        }
        
        public Integer getDiscountValue() {
            return discountValue;
        }
        
        public void setDiscountValue(Integer discountValue) {
            this.discountValue = discountValue;
        }
    }
    
    public static class CouponInfo {
        private String discountType;
        private Integer discountValue;
        
        public CouponInfo(String discountType, Integer discountValue) {
            this.discountType = discountType;
            this.discountValue = discountValue;
        }
        
        public String getDiscountType() {
            return discountType;
        }
        
        public Integer getDiscountValue() {
            return discountValue;
        }
    }
}