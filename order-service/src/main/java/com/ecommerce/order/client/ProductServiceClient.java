package com.ecommerce.order.client;

import com.ecommerce.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${external.services.product-service.url}")
    private String productServiceUrl;
    
    public ProductInfo getProductInfo(Long productId) {
        if (productId == null || productId <= 0) {
            log.error("Invalid productId: {}", productId);
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다: " + productId);
        }
        
        try {
            log.debug("Requesting product info: productId={}, url={}", productId, productServiceUrl);
            
            ApiResponse<ProductInfo> response = webClientBuilder.build()
                    .get()
                    .uri(productServiceUrl + "/api/v1/internal/products/{productId}", productId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductInfo>>() {})
                    .block();
            
            if (response == null || response.getData() == null) {
                log.error("Empty response from product service: productId={}", productId);
                throw new RuntimeException("상품 정보를 찾을 수 없습니다");
            }
            
            log.debug("Successfully retrieved product info: productId={}, name={}", 
                    productId, response.getData().getName());
            
            return response.getData();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest e) {
            log.error("Bad request to product service: productId={}, status={}, body={}", 
                    productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("존재하지 않는 상품입니다: " + productId);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.NotFound e) {
            log.error("Product not found: productId={}", productId);
            throw new IllegalArgumentException("존재하지 않는 상품입니다: " + productId);
        } catch (Exception e) {
            log.error("Failed to get product info: productId={}", productId, e);
            throw new RuntimeException("상품 정보 조회 중 오류가 발생했습니다", e);
        }
    }
    
    // Inner classes - ApiResponseWrapper 제거
    public static class ProductInfo {
        private String name;
        private BigDecimal price;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}