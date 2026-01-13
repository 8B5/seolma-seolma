package com.ecommerce.order.controller;

import com.ecommerce.common.annotation.AdminOnly;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 주문 관리", description = "관리자용 주문 관리 API")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@AdminOnly
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    @Operation(summary = "전체 주문 목록 조회", description = "관리자가 모든 주문을 조회합니다")
    @GetMapping
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @Parameter(description = "사용자 ID") @RequestParam(required = false) String userId,
            @Parameter(description = "주문 상태 (PAYMENT_COMPLETED, PREPARING, SHIPPING, DELIVERED, CANCELLED)",
                      schema = @Schema(allowableValues = {"PAYMENT_COMPLETED", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED"})) 
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "상품 ID") @RequestParam(required = false) Long productId,
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<OrderResponse> response = adminOrderService.getAllOrders(userId, status, productId, pageable);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "주문 상세 조회", description = "관리자가 특정 주문의 상세 정보를 조회합니다")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId) {
        
        OrderResponse response = adminOrderService.getOrderById(orderId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "주문 상태 변경", description = "관리자가 주문의 상태를 변경합니다")
    @PatchMapping("/{orderId}/status")
    public ApiResponse<Void> changeOrderStatus(
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "변경할 상태 (PAYMENT_COMPLETED, PREPARING, SHIPPING, DELIVERED, CANCELLED)", 
                      required = true,
                      schema = @Schema(allowableValues = {"PAYMENT_COMPLETED", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED"})) 
            @RequestParam OrderStatus status) {
        
        adminOrderService.changeOrderStatus(orderId, status);
        return ApiResponse.success();
    }
    
    @Operation(summary = "주문 통계", description = "주문 관련 통계를 조회합니다")
    @GetMapping("/statistics")
    public ApiResponse<OrderStatisticsResponse> getOrderStatistics(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {
        
        OrderStatisticsResponse response = adminOrderService.getOrderStatistics(startDate, endDate);
        return ApiResponse.success(response);
    }
    
    // Inner class for statistics response
    public static class OrderStatisticsResponse {
        private final Long totalOrders;
        private final Long completedOrders;
        private final Long cancelledOrders;
        private final Long shippingOrders;
        private final String totalRevenue;
        private final Double completionRate;
        
        public OrderStatisticsResponse(Long totalOrders, Long completedOrders, Long cancelledOrders, 
                                     Long shippingOrders, String totalRevenue) {
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.shippingOrders = shippingOrders;
            this.totalRevenue = totalRevenue;
            this.completionRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0.0;
        }
        
        // Getters
        public Long getTotalOrders() { return totalOrders; }
        public Long getCompletedOrders() { return completedOrders; }
        public Long getCancelledOrders() { return cancelledOrders; }
        public Long getShippingOrders() { return shippingOrders; }
        public String getTotalRevenue() { return totalRevenue; }
        public Double getCompletionRate() { return completionRate; }
    }
}