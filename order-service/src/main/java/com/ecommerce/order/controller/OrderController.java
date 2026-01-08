package com.ecommerce.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityUtils;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderCreateRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문", description = "주문 및 결제 관리 API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성하고 결제를 처리합니다")
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        OrderResponse response = orderService.createOrder(request, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "내 주문 목록 조회", description = "사용자의 주문 목록을 조회합니다")
    @GetMapping("/my")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        OrderResponse response = orderService.getOrder(orderId, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "주문 취소", description = "주문을 취소합니다")
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        orderService.cancelOrder(orderId, userId);
        return ApiResponse.success();
    }
    
    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다 (관리자용)")
    @PatchMapping("/{orderId}/status")
    public ApiResponse<Void> changeOrderStatus(
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "변경할 상태", required = true) @RequestParam OrderStatus status) {
        
        orderService.changeOrderStatus(orderId, status);
        return ApiResponse.success();
    }
}