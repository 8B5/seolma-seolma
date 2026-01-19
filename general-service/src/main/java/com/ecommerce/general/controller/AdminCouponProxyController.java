package com.ecommerce.general.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Admin Coupon API를 coupon-service로 프록시하는 컨트롤러
 * ALB 라우팅 문제 해결을 위한 임시 솔루션
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponProxyController {
    
    private final RestTemplate restTemplate;
    
    @Value("${external.coupon-service.url}")
    private String couponServiceUrl;
    
    /**
     * 쿠폰 템플릿 목록 조회를 coupon-service로 프록시
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getCouponTemplates() {
        String url = couponServiceUrl + "/api/v1/admin/coupons/templates";
        return restTemplate.getForEntity(url, Object.class);
    }
    
    /**
     * 쿠폰 템플릿 생성을 coupon-service로 프록시
     */
    @PostMapping("/templates")
    public ResponseEntity<?> createCouponTemplate(@RequestBody Object request) {
        String url = couponServiceUrl + "/api/v1/admin/coupons/templates";
        return restTemplate.postForEntity(url, request, Object.class);
    }
    
    /**
     * 쿠폰 템플릿 수정을 coupon-service로 프록시
     */
    @PutMapping("/templates/{templateId}")
    public ResponseEntity<?> updateCouponTemplate(@PathVariable Long templateId, @RequestBody Object request) {
        String url = couponServiceUrl + "/api/v1/admin/coupons/templates/" + templateId;
        restTemplate.put(url, request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 쿠폰 템플릿 삭제를 coupon-service로 프록시
     */
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<?> deleteCouponTemplate(@PathVariable Long templateId) {
        String url = couponServiceUrl + "/api/v1/admin/coupons/templates/" + templateId;
        restTemplate.delete(url);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 기타 모든 admin coupon API를 프록시
     */
    @RequestMapping("/**")
    public ResponseEntity<?> proxyAllAdminCouponRequests(
            @RequestBody(required = false) Object request,
            @RequestParam(required = false) String params) {
        // 실제 구현에서는 더 정교한 프록시 로직이 필요
        return ResponseEntity.ok("Admin coupon API proxy - implement as needed");
    }
}