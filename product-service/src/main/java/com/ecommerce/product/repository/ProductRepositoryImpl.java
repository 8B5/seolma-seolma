package com.ecommerce.product.repository;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.QProduct;
import com.ecommerce.product.dto.ProductSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        QProduct product = QProduct.product;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 기본 조건: 삭제되지 않은 상품
        builder.and(product.isDeleted.eq(false));
        
        // 상품명 검색
        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(product.name.containsIgnoreCase(condition.getKeyword()));
        }
        
        // 판매자 ID
        if (condition.getSellerId() != null) {
            builder.and(product.sellerId.eq(condition.getSellerId()));
        }
        
        // 가격 범위
        if (condition.getMinPrice() != null) {
            builder.and(product.price.goe(condition.getMinPrice()));
        }
        if (condition.getMaxPrice() != null) {
            builder.and(product.price.loe(condition.getMaxPrice()));
        }
        
        // 데이터 조회 (이미지 fetch join)
        List<Product> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.images).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(builder);
        
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    @Override
    public Page<Product> searchProductsForAdmin(ProductSearchCondition condition, Boolean includeDeleted, Pageable pageable) {
        QProduct product = QProduct.product;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 삭제된 상품 포함 여부
        if (includeDeleted == null || !includeDeleted) {
            builder.and(product.isDeleted.eq(false));
        }
        
        // 상품명 검색
        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(product.name.containsIgnoreCase(condition.getKeyword()));
        }
        
        // 판매자 ID
        if (condition.getSellerId() != null) {
            builder.and(product.sellerId.eq(condition.getSellerId()));
        }
        
        // 가격 범위
        if (condition.getMinPrice() != null) {
            builder.and(product.price.goe(condition.getMinPrice()));
        }
        if (condition.getMaxPrice() != null) {
            builder.and(product.price.loe(condition.getMaxPrice()));
        }
        
        // 데이터 조회 (이미지 fetch join)
        List<Product> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.images).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(builder);
        
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}