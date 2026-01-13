package com.ecommerce.order.dto;

import com.ecommerce.order.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "주문 생성 요청")
public class OrderCreateRequest {
    
    @NotBlank(message = "주문자 이름은 필수입니다")
    @Size(max = 50, message = "주문자 이름은 50자 이하여야 합니다")
    @Schema(description = "주문자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ordererName;
    
    @NotBlank(message = "배송지 주소는 필수입니다")
    @Size(max = 500, message = "배송지 주소는 500자 이하여야 합니다")
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shippingAddress;
    
    @NotNull(message = "상품 ID는 필수입니다")
    @Min(value = 1, message = "상품 ID는 1 이상이어야 합니다")
    @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;
    
    @NotNull(message = "주문 수량은 필수입니다")
    @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다")
    @Schema(description = "주문 수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
    
    @Schema(description = "적용할 쿠폰 ID (선택사항)", example = "1")
    private Long appliedCouponId;  // 선택사항
    
    @NotNull(message = "결제 방법은 필수입니다")
    @Schema(description = "결제 방법", 
            example = "CARD", 
            allowableValues = {"CARD", "TRANS", "VIRTUAL_ACCOUNT", "PHONE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethod paymentMethod;
    
    public OrderCreateRequest(String ordererName, String shippingAddress, Long productId, 
                            Integer quantity, Long appliedCouponId, PaymentMethod paymentMethod) {
        this.ordererName = ordererName;
        this.shippingAddress = shippingAddress;
        this.productId = productId;
        this.quantity = quantity;
        this.appliedCouponId = appliedCouponId;
        this.paymentMethod = paymentMethod;
    }
}