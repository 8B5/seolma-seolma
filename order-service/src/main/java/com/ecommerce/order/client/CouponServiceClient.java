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
                    .uri(couponServiceUrl + "/api/v1/coupons/{couponId}/use", couponId)
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
            // 실제로는 쿠폰 정보 조회 API 호출
            // 현재는 임시로 더미 데이터 반환
            return new CouponInfo("PERCENT", 10);
        } catch (Exception e) {
            log.error("Failed to get coupon info: couponId={}, userId={}", couponId, userId, e);
            throw new RuntimeException("쿠폰 정보 조회 실패", e);
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