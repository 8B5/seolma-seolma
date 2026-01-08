package com.ecommerce.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;  // 주문자 ID (VARCHAR(50))
    
    @Column(name = "orderer_name", nullable = false, length = 50)
    private String ordererName;
    
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_snapshot_name", nullable = false)
    private String productSnapshotName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "applied_coupon_id")
    private Long appliedCouponId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PAYMENT_COMPLETED;
    
    @CreatedDate
    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Builder
    public Order(String userId, String ordererName, String shippingAddress, 
                Long productId, String productSnapshotName, Integer quantity, 
                BigDecimal totalPrice, Long appliedCouponId, PaymentMethod paymentMethod) {
        this.userId = userId;
        this.ordererName = ordererName;
        this.shippingAddress = shippingAddress;
        this.productId = productId;
        this.productSnapshotName = productSnapshotName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.appliedCouponId = appliedCouponId;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }
    
    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
    
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    public boolean canCancel() {
        return this.status == OrderStatus.PAYMENT_COMPLETED || this.status == OrderStatus.PREPARING;
    }
}