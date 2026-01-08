package com.ecommerce.order.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.order.client.CouponServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderCreateRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.exception.OrderResultCode;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final CouponServiceClient couponServiceClient;
    
    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String userId) {
        // 0. 입력값 검증
        validateOrderRequest(request);
        
        // 1. 상품 정보 조회
        ProductServiceClient.ProductInfo productInfo = productServiceClient.getProductInfo(request.getProductId());
        
        // 2. 쿠폰 사용 처리
        if (request.getAppliedCouponId() != null) {
            couponServiceClient.useCoupon(request.getAppliedCouponId(), userId);
        }
        
        // 3. 총 금액 계산
        BigDecimal totalPrice = calculateTotalPrice(productInfo.getPrice(), request.getQuantity(), request.getAppliedCouponId(), userId);
        
        // 4. 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .ordererName(request.getOrdererName())
                .shippingAddress(request.getShippingAddress())
                .productId(request.getProductId())
                .productSnapshotName(productInfo.getName())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .appliedCouponId(request.getAppliedCouponId())
                .paymentMethod(request.getPaymentMethod())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created: orderId={}, userId={}, productId={}, totalPrice={}", 
                savedOrder.getId(), userId, request.getProductId(), totalPrice);
        
        return OrderResponse.from(savedOrder);
    }
    
    /**
     * 사용자별 주문 목록 조회
     */
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        // 유효한 정렬 필드만 허용
        Pageable validatedPageable = validateAndFixPageable(pageable);
        
        return orderRepository.findByUserIdOrderByOrderedAtDesc(userId, validatedPageable)
                .map(OrderResponse::from);
    }
    
    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BusinessException(OrderResultCode.ORDER_NOT_FOUND));
        
        return OrderResponse.from(order);
    }
    
    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BusinessException(OrderResultCode.ORDER_NOT_FOUND));
        
        if (!order.canCancel()) {
            throw new BusinessException(OrderResultCode.ORDER_CANNOT_CANCEL);
        }
        
        order.cancel();
        
        log.info("Order cancelled: orderId={}, userId={}", orderId, userId);
    }
    
    /**
     * 주문 상태 변경 (관리자용)
     */
    @Transactional
    public void changeOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderResultCode.ORDER_NOT_FOUND));
        
        order.changeStatus(status);
        
        log.info("Order status changed: orderId={}, newStatus={}", orderId, status);
    }
    
    // Private helper methods
    private BigDecimal calculateTotalPrice(BigDecimal productPrice, Integer quantity, Long couponId, String userId) {
        BigDecimal totalPrice = productPrice.multiply(BigDecimal.valueOf(quantity));
        
        // 쿠폰 할인 적용
        if (couponId != null) {
            CouponServiceClient.CouponInfo couponInfo = couponServiceClient.getCouponInfo(couponId, userId);
            
            if ("PERCENT".equals(couponInfo.getDiscountType())) {
                BigDecimal discountRate = BigDecimal.valueOf(100 - couponInfo.getDiscountValue()).divide(BigDecimal.valueOf(100));
                totalPrice = totalPrice.multiply(discountRate);
            } else if ("FIXED_AMOUNT".equals(couponInfo.getDiscountType())) {
                totalPrice = totalPrice.subtract(BigDecimal.valueOf(couponInfo.getDiscountValue()));
                if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    totalPrice = BigDecimal.ZERO;
                }
            }
        }
        
        return totalPrice;
    }
    
    private void validateOrderRequest(OrderCreateRequest request) {
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new BusinessException(OrderResultCode.INVALID_PRODUCT_ID);
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException(OrderResultCode.INVALID_QUANTITY);
        }
        
        if (request.getAppliedCouponId() != null && request.getAppliedCouponId() <= 0) {
            throw new BusinessException(OrderResultCode.INVALID_COUPON_ID);
        }
    }
    
    private Pageable validateAndFixPageable(Pageable pageable) {
        // Order 엔티티의 유효한 필드명들
        List<String> validSortFields = List.of("id", "userId", "ordererName", "productId", 
                "productSnapshotName", "quantity", "totalPrice", "appliedCouponId", 
                "paymentMethod", "status", "orderedAt", "cancelledAt");
        
        if (pageable.getSort().isSorted()) {
            // 정렬 필드 검증
            boolean hasInvalidSort = pageable.getSort().stream()
                    .anyMatch(order -> !validSortFields.contains(order.getProperty()));
            
            if (hasInvalidSort) {
                // 유효하지 않은 정렬 필드가 있으면 기본 정렬(orderedAt DESC)로 변경
                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.DESC, "orderedAt"));
            }
        }
        
        return pageable;
    }
}