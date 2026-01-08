package com.ecommerce.product.repository;

import com.ecommerce.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    // 상품별 이미지 조회 (삭제되지 않은 것만)
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isDeleted = false ORDER BY pi.id ASC")
    List<ProductImage> findByProductIdAndIsDeletedFalse(@Param("productId") Long productId);
    
    // 상품의 대표 이미지 조회 (첫 번째 이미지)
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isDeleted = false ORDER BY pi.id ASC LIMIT 1")
    ProductImage findFirstByProductIdAndIsDeletedFalse(@Param("productId") Long productId);
    
    // 상품별 이미지 개수 조회
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isDeleted = false")
    Long countByProductIdAndIsDeletedFalse(@Param("productId") Long productId);
    
    // 상품별 모든 이미지 소프트 삭제
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isDeleted = true WHERE pi.product.id = :productId AND pi.isDeleted = false")
    int softDeleteByProductId(@Param("productId") Long productId);
    
    // 상품별 모든 이미지 조회 (삭제된 것 포함)
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.id ASC")
    List<ProductImage> findAllByProductId(@Param("productId") Long productId);
}