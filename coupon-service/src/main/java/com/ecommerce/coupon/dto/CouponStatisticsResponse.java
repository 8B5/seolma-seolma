package com.ecommerce.coupon.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CouponStatisticsResponse {
    
    private final Long templateId;
    private final String templateTitle;
    private final Long totalIssued;
    private final Long totalUsed;
    private final Long totalRemaining;
    private final Double usageRate;
    
    public static CouponStatisticsResponse of(Long templateId, String templateTitle, 
                                            Long totalIssued, Long totalUsed) {
        Long totalRemaining = totalIssued - totalUsed;
        Double usageRate = totalIssued > 0 ? (double) totalUsed / totalIssued * 100 : 0.0;
        
        return new CouponStatisticsResponse(
                templateId,
                templateTitle,
                totalIssued,
                totalUsed,
                totalRemaining,
                usageRate
        );
    }
}