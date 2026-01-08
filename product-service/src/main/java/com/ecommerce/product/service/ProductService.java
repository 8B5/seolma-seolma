package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.exception.ProductResultCode;
import com.ecommerce.product.repository.ProductImageRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String sellerId) {
        Product product = Product.builder()
                .name(request.getName())
                .sellerId(sellerId)
                .price(request.getPrice())
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created: id={}, name={}, sellerId={}", 
                savedProduct.getId(), savedProduct.getName(), sellerId);
        
        return ProductResponse.from(savedProduct);
    }
    
    public ProductResponse getProduct(Long productId) {
        Product product = findProductById(productId);
        return ProductResponse.from(product);
    }
    
    public Page<ProductResponse> getProducts(Pageable pageable) {
        // 유효한 정렬 필드만 허용
        Pageable validatedPageable = validateAndFixPageable(pageable);
        return productRepository.findAvailableProducts(validatedPageable)
                .map(ProductResponse::from);
    }
    
    public Page<ProductResponse> getProductsBySeller(String sellerId, Pageable pageable) {
        return productRepository.findBySellerIdAndIsDeletedFalse(sellerId, pageable)
                .map(ProductResponse::from);
    }
    
    public Page<ProductResponse> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        return productRepository.searchProducts(condition, pageable)
                .map(ProductResponse::from);
    }
    
    public Page<ProductResponse> searchProductsForAdmin(ProductSearchCondition condition, Boolean includeDeleted, Pageable pageable) {
        return productRepository.searchProductsForAdmin(condition, includeDeleted, pageable)
                .map(ProductResponse::from);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request, String sellerId) {
        Product product = findProductById(productId);
        
        // 권한 검증
        validateSellerAccess(product, sellerId);
        
        product.updateBasicInfo(request.getName(), request.getPrice());
        
        log.info("Product updated: id={}, name={}, sellerId={}", 
                productId, request.getName(), sellerId);
        
        return ProductResponse.from(product);
    }
    
    @Transactional
    public ProductResponse updateProductByAdmin(Long productId, ProductUpdateRequest request, String adminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductResultCode.PRODUCT_NOT_FOUND));
        
        product.updateBasicInfo(request.getName(), request.getPrice());
        
        log.info("Product updated by admin: id={}, name={}, adminId={}", 
                productId, request.getName(), adminId);
        
        return ProductResponse.from(product);
    }
    
    @Transactional
    public void deleteProduct(Long productId, String sellerId) {
        Product product = findProductById(productId);
        
        // 권한 검증
        validateSellerAccess(product, sellerId);
        
        if (product.getIsDeleted()) {
            throw new BusinessException(ProductResultCode.PRODUCT_ALREADY_DELETED);
        }
        
        // 상품 소프트 삭제
        product.delete();
        
        // 해당 상품의 모든 이미지도 소프트 삭제
        int deletedImageCount = productImageRepository.softDeleteByProductId(productId);
        
        log.info("Product deleted: productId={}, sellerId={}, deletedImageCount={}", 
                productId, sellerId, deletedImageCount);
    }
    
    @Transactional
    public void restoreProduct(Long productId, String adminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductResultCode.PRODUCT_NOT_FOUND));
        
        if (!product.getIsDeleted()) {
            throw new BusinessException(ProductResultCode.PRODUCT_NOT_DELETED);
        }
        
        product.restore();
        
        log.info("Product restored by admin: productId={}, adminId={}", productId, adminId);
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(ProductResultCode.PRODUCT_NOT_FOUND));
    }
    
    private void validateSellerAccess(Product product, String sellerId) {
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(ProductResultCode.UNAUTHORIZED_PRODUCT_ACCESS);
        }
    }
    
    private Pageable validateAndFixPageable(Pageable pageable) {
        // Product 엔티티의 유효한 필드명들
        List<String> validSortFields = List.of("id", "name", "price", "sellerId", "createdAt", "isDeleted");
        
        if (pageable.getSort().isSorted()) {
            // 정렬 필드 검증
            boolean hasInvalidSort = pageable.getSort().stream()
                    .anyMatch(order -> !validSortFields.contains(order.getProperty()));
            
            if (hasInvalidSort) {
                // 유효하지 않은 정렬 필드가 있으면 기본 정렬(id)로 변경
                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.ASC, "id"));
            }
        }
        
        return pageable;
    }
}