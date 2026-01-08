package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.dto.ProductCreateRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.ProductUpdateRequest;
import com.ecommerce.product.exception.ProductResultCode;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", "상품 설명", new BigDecimal("10000"));
        String sellerId = "seller1";
        
        Product savedProduct = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sellerId(sellerId)
                .price(request.getPrice())
                .build();
        
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        
        // when
        ProductResponse response = productService.createProduct(request, sellerId);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10000"));
        assertThat(response.getSellerId()).isEqualTo(sellerId);
    }
}
        
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        
        // when
        ProductResponse response = productService.createProduct(request, sellerId);
        
        // then
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10000"));
        assertThat(response.getSellerId()).isEqualTo(sellerId);
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .sellerId(1L)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when
        ProductResponse response = productService.getProduct(productId);
        
        // then
        assertThat(response.getName()).isEqualTo("테스트 상품");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10000"));
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 조회 실패")
    void getProduct_NotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ProductResultCode.PRODUCT_NOT_FOUND);
    }
    
    @Test
    @DisplayName("상품 정보 수정 성공")
    void updateProduct_Success() {
        // given
        Long productId = 1L;
        Long sellerId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "수정된 상품명", "수정된 설명", new BigDecimal("15000"));
        
        Product product = Product.builder()
                .name("원래 상품명")
                .description("원래 설명")
                .sellerId(sellerId)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when
        ProductResponse response = productService.updateProduct(productId, request, sellerId);
        
        // then
        assertThat(response.getName()).isEqualTo("수정된 상품명");
        assertThat(response.getDescription()).isEqualTo("수정된 설명");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("15000"));
    }
    
    @Test
    @DisplayName("권한 없는 사용자의 상품 수정 실패")
    void updateProduct_UnauthorizedAccess() {
        // given
        Long productId = 1L;
        Long sellerId = 1L;
        Long unauthorizedSellerId = 2L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "수정된 상품명", "수정된 설명", new BigDecimal("15000"));
        
        Product product = Product.builder()
                .name("원래 상품명")
                .description("원래 설명")
                .sellerId(sellerId)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when & then
        assertThatThrownBy(() -> productService.updateProduct(productId, request, unauthorizedSellerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ProductResultCode.UNAUTHORIZED_PRODUCT_ACCESS);
    }
    
    @Test
    @DisplayName("재고 차감 성공")
    void decreaseStock_Success() {
        // given
        Long productId = 1L;
        Integer decreaseQuantity = 10;
        
        Product product = Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .sellerId(1L)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when
        assertThatCode(() -> productService.decreaseStock(productId, decreaseQuantity))
                .doesNotThrowAnyException();
        
        // then
        assertThat(product.getStockQuantity()).isEqualTo(90);
    }
    
    @Test
    @DisplayName("재고 부족으로 차감 실패")
    void decreaseStock_InsufficientStock() {
        // given
        Long productId = 1L;
        Integer decreaseQuantity = 150;
        
        Product product = Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .sellerId(1L)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when & then
        assertThatThrownBy(() -> productService.decreaseStock(productId, decreaseQuantity))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ProductResultCode.INSUFFICIENT_STOCK);
    }
    
    @Test
    @DisplayName("재고 수정 성공")
    void updateStock_Success() {
        // given
        Long productId = 1L;
        Long sellerId = 1L;
        StockUpdateRequest request = new StockUpdateRequest(200);
        
        Product product = Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .sellerId(sellerId)
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        
        given(productRepository.findByIdAndIsDeletedFalse(productId)).willReturn(Optional.of(product));
        
        // when
        assertThatCode(() -> productService.updateStock(productId, request, sellerId))
                .doesNotThrowAnyException();
        
        // then
        assertThat(product.getStockQuantity()).isEqualTo(200);
    }
}