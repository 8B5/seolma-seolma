package com.ecommerce.product.repository;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    
    /**
     * 복합 조건으로 상품 검색
     */
    Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable);
    
    /**
     * 관리자용 상품 검색 (삭제된 상품 포함 가능)
     */
    Page<Product> searchProductsForAdmin(ProductSearchCondition condition, Boolean includeDeleted, Pageable pageable);
}