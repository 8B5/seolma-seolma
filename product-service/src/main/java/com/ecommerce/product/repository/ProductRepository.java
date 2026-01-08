package com.ecommerce.product.repository;

import com.ecommerce.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    
    // 삭제되지 않은 상품 조회
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    
    // 판매자별 상품 조회
    Page<Product> findBySellerIdAndIsDeletedFalse(String sellerId, Pageable pageable);
    
    // 상품명으로 검색 (삭제되지 않은 상품만)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.isDeleted = false")
    Page<Product> findByNameContainingAndIsDeletedFalse(@Param("keyword") String keyword, Pageable pageable);
    
    // 가격 범위로 검색
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isDeleted = false")
    Page<Product> findByPriceBetweenAndIsDeletedFalse(
            @Param("minPrice") BigDecimal minPrice, 
            @Param("maxPrice") BigDecimal maxPrice, 
            Pageable pageable);
    
    // 활성 상품 조회 (삭제되지 않은 상품) - Pageable의 정렬 사용
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findAvailableProducts(Pageable pageable);
    
    // 판매자의 활성 상품 수 조회
    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sellerId AND p.isDeleted = false")
    Long countActiveProductsBySellerId(@Param("sellerId") String sellerId);
    
    // 최신 상품 조회
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);
}