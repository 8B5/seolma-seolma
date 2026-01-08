package com.ecommerce.order.dto;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class OrderResponse {
    
    private final Long id;
    private final String userId;
    private final String ordererName;
    private final String shippingAddress;
    private final Long productId;
    private final String productSnapshotName;
    private final Integer quantity;
    private final BigDecimal totalPrice;
    private final Long appliedCouponId;
    private final PaymentMethod paymentMethod;
    private final String paymentMethodDisplayName;
    private final OrderStatus status;
    private final String statusDisplayName;
    private final LocalDateTime orderedAt;
    private final LocalDateTime cancelledAt;
    private final Boolean canCancel;
    
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrdererName(),
                order.getShippingAddress(),
                order.getProductId(),
                order.getProductSnapshotName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getAppliedCouponId(),
                order.getPaymentMethod(),
                order.getPaymentMethod().getDisplayName(),
                order.getStatus(),
                order.getStatus().getDisplayName(),
                order.getOrderedAt(),
                order.getCancelledAt(),
                order.canCancel()
        );
    }
}