package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 사용자별 주문 목록 조회 (최신순)
    Page<Order> findByUserIdOrderByOrderedAtDesc(String userId, Pageable pageable);
    
    // 사용자별 특정 상태 주문 조회
    Page<Order> findByUserIdAndStatusOrderByOrderedAtDesc(String userId, OrderStatus status, Pageable pageable);
    
    // 사용자의 특정 주문 조회
    Optional<Order> findByIdAndUserId(Long orderId, String userId);
    
    // 사용자별 주문 개수 조회
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    // 특정 상품의 주문 내역 조회
    List<Order> findByProductIdOrderByOrderedAtDesc(Long productId);
    
    // 관리자용 주문 검색
    @Query("SELECT o FROM Order o WHERE " +
           "(:userId IS NULL OR o.userId = :userId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:productId IS NULL OR o.productId = :productId) " +
           "ORDER BY o.orderedAt DESC")
    Page<Order> findAllWithConditions(@Param("userId") String userId, 
                                     @Param("status") OrderStatus status, 
                                     @Param("productId") Long productId, 
                                     Pageable pageable);
    
    // 기간별 주문 수 조회
    @Query("SELECT COUNT(o) FROM Order o WHERE " +
           "(:startDate IS NULL OR o.orderedAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderedAt <= :endDate)")
    Long countOrdersByPeriod(@Param("startDate") LocalDateTime startDate, 
                            @Param("endDate") LocalDateTime endDate);
    
    // 기간별 상태별 주문 수 조회
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND " +
           "(:startDate IS NULL OR o.orderedAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderedAt <= :endDate)")
    Long countOrdersByStatusAndPeriod(@Param("status") OrderStatus status,
                                     @Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // 기간별 총 매출 조회
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status != 'CANCELLED' AND " +
           "(:startDate IS NULL OR o.orderedAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderedAt <= :endDate)")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
}