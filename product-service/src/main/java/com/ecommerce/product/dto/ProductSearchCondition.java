package com.ecommerce.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchCondition {
    
    private String keyword;           // 상품명 검색 키워드
    private String sellerId;           // 판매자 ID
    private BigDecimal minPrice;     // 최소 가격
    private BigDecimal maxPrice;     // 최대 가격
    
    public ProductSearchCondition(String keyword, String sellerId, 
                                BigDecimal minPrice, BigDecimal maxPrice) {
        this.keyword = keyword;
        this.sellerId = sellerId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}