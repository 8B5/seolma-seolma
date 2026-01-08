package com.ecommerce.order.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.order.controller.AdminOrderController.OrderStatisticsResponse;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    
    public Page<OrderResponse> getAllOrders(String userId, OrderStatus status, Long productId, Pageable pageable) {
        // 유효한 정렬 필드만 허용
        Pageable validatedPageable = validateAndFixPageable(pageable);
        
        return orderRepository.findAllWithConditions(userId, status, productId, validatedPageable)
                .map(OrderResponse::from);
    }
    
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderResultCode.ORDER_NOT_FOUND));
        
        return OrderResponse.from(order);
    }
    
    @Transactional
    public void changeOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderResultCode.ORDER_NOT_FOUND));
        
        order.changeStatus(status);
        
        log.info("Order status changed by admin: orderId={}, newStatus={}", orderId, status);
    }
    
    public OrderStatisticsResponse getOrderStatistics(String startDate, String endDate) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null) {
            startDateTime = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59);
        }
        
        Long totalOrders = orderRepository.countOrdersByPeriod(startDateTime, endDateTime);
        Long completedOrders = orderRepository.countOrdersByStatusAndPeriod(OrderStatus.DELIVERED, startDateTime, endDateTime);
        Long cancelledOrders = orderRepository.countOrdersByStatusAndPeriod(OrderStatus.CANCELLED, startDateTime, endDateTime);
        Long shippingOrders = orderRepository.countOrdersByStatusAndPeriod(OrderStatus.SHIPPING, startDateTime, endDateTime);
        
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(startDateTime, endDateTime);
        String totalRevenueStr = totalRevenue != null ? totalRevenue.toString() : "0";
        
        return new OrderStatisticsResponse(totalOrders, completedOrders, cancelledOrders, shippingOrders, totalRevenueStr);
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