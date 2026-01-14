# 최종 프로젝트 구조

## 📋 개요

EC2 2대 구조에 맞게 서비스를 **General Service**와 **Coupon Service** 2개로 구성했습니다.

---

## 🏗️ 서비스 구성

### General Service (포트 8080)
- **포함 기능**: User + Product + Order
- **데이터베이스**: common_db
- **배포 위치**: EC2-2

### Coupon Service (포트 8081)
- **포함 기능**: Coupon 발급 및 관리
- **데이터베이스**: coupon_db
- **배포 위치**: EC2-1

---

## 🔧 구현 방식

### Gradle 의존성 통합 (현재 방식)

**general-service/build.gradle:**
```gradle
dependencies {
    // 다른 서비스들을 의존성으로 추가
    implementation project(':user-service')
    implementation project(':product-service')
    implementation project(':order-service')
    implementation project(':common-lib')
    
    // 기타 의존성...
}
```

**장점:**
- ✅ 소스 복사 불필요
- ✅ 자동으로 최신 코드 반영
- ✅ Bean 충돌 없음
- ✅ 관리가 쉬움

### Application 클래스

**GeneralServiceApplication.java:**
```java
@SpringBootApplication(scanBasePackages = "com.ecommerce")
@EntityScan(basePackages = {
    "com.ecommerce.user.domain",
    "com.ecommerce.product.domain",
    "com.ecommerce.order.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.ecommerce.user.repository",
    "com.ecommerce.product.repository",
    "com.ecommerce.order.repository"
})
@EnableJpaAuditing
public class GeneralServiceApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(GeneralServiceApplication.class, args);
    }
}
```

---

## 📁 디렉토리 구조

```
ecommerce-msa/
├── common-lib/                 # 공통 라이브러리
│   ├── security/              # JWT, @AdminOnly
│   ├── response/              # 공통 응답
│   ├── exception/             # 예외 처리
│   └── util/                  # 유틸리티
│
├── general-service/           # 통합 서비스 (실행)
│   ├── src/main/java/com/ecommerce/general/
│   │   └── GeneralServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle          # user/product/order 의존성
│
├── user-service/              # 회원 서비스 (소스 코드)
│   ├── domain/               # User 엔티티
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── config/               # UserSecurityConfig
│
├── product-service/           # 상품 서비스 (소스 코드)
│   ├── domain/               # Product, ProductImage
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── config/               # WebConfig (이미지 서빙)
│
├── order-service/             # 주문 서비스 (소스 코드)
│   ├── domain/               # Order 엔티티
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── client/               # CouponServiceClient
│
├── coupon-service/            # 쿠폰 서비스 (실행)
│   ├── domain/               # CouponTemplate, Coupon
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── CouponServiceApplication.java
│
├── deployment/               # 배포 관련
│   ├── SIMPLE_DEPLOY_GUIDE.md
│   ├── nginx.conf
│   └── tomcat-setup.sh
│
├── build-general.bat         # General Service 빌드
├── build-coupon.bat          # Coupon Service 빌드
└── README.md
```

---

## 🚀 실행 방법

### 개발 환경

```cmd
REM General Service 실행
gradlew :general-service:bootRun

REM Coupon Service 실행 (별도 터미널)
gradlew :coupon-service:bootRun
```

### WAR 빌드

```cmd
REM General Service 빌드
build-general.bat

REM Coupon Service 빌드
build-coupon.bat
```

**빌드 결과:**
- `general-service/build/libs/general-service-1.0.0.war`
- `coupon-service/build/libs/coupon-service-1.0.0.war`

---

## 🌐 포트 구성

### 개발 환경
- **General Service**: `8080`
- **Coupon Service**: `8081`

### 운영 환경
- **EC2-1**: Coupon Service (`8081`)
- **EC2-2**: General Service (`8080`)

---

## 📡 API 엔드포인트

### General Service (8080)
```
POST   /api/v1/users/auth/signup
POST   /api/v1/users/auth/login
GET    /api/v1/users/me

GET    /api/v1/products
POST   /api/v1/admin/products
GET    /api/v1/internal/products/{id}

POST   /api/v1/orders
GET    /api/v1/orders/my
GET    /api/v1/admin/orders
```

### Coupon Service (8081)
```
GET    /api/v1/coupons/templates/available
POST   /api/v1/coupons/issue
GET    /api/v1/coupons/my

POST   /api/v1/admin/coupons/templates
GET    /api/v1/admin/coupons/templates
GET    /api/v1/internal/coupons/{id}
```

---

## 🔗 서비스 간 통신

### Order Service → Coupon Service

**CouponServiceClient.java:**
```java
@Component
public class CouponServiceClient {
    @Value("${external.services.coupon-service.url}")
    private String couponServiceUrl;
    
    private final WebClient webClient;
    
    public CouponInfo getCouponInfo(Long couponId, String userId) {
        return webClient.get()
            .uri(couponServiceUrl + "/api/v1/internal/coupons/{couponId}", couponId)
            .header("X-User-Id", userId)
            .retrieve()
            .bodyToMono(CouponResponse.class)
            .map(response -> new CouponInfo(
                response.getDiscountType(),
                response.getDiscountValue()
            ))
            .block();
    }
}
```

---

## 🔐 보안 설정

### UserSecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class UserSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // Internal API (서비스 간 통신)
            .requestMatchers("/api/v1/internal/**").permitAll()
            // Static resources (이미지)
            .requestMatchers("/images/**").permitAll()
            // Auth endpoints
            .requestMatchers("/api/v1/auth/**", "/api/v1/users/auth/**").permitAll()
            // Public endpoints
            .requestMatchers("/api/v1/products/**").permitAll()
            // Authenticated endpoints
            .requestMatchers("/api/v1/orders/**").authenticated()
            .requestMatchers("/api/v1/admin/**").authenticated()
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

---

## 📊 데이터베이스

### common_db (General Service)
```sql
-- 사용자
users (user_id, password, user_name, role, created_at)

-- 상품
products (product_id, name, seller_id, price, is_deleted, created_at)
product_images (image_id, product_id, image_url, is_deleted, created_at)

-- 주문
orders (order_id, user_id, product_id, quantity, total_price, 
        applied_coupon_id, status, ordered_at, cancelled_at)
```

### coupon_db (Coupon Service)
```sql
-- 쿠폰 템플릿
coupon_templates (template_id, title, discount_type, discount_value,
                  started_at, finished_at, is_limited, total_quantity, is_deleted)

-- 발급된 쿠폰
coupons (coupon_id, template_id, user_id, is_used, used_at, issued_at)
```

---

## 🎯 배포 구조

### ALB 라우팅
```
/api/v1/users/**    → EC2-2 (General Service)
/api/v1/products/** → EC2-2 (General Service)
/api/v1/orders/**   → EC2-2 (General Service)
/api/v1/coupons/**  → EC2-1 (Coupon Service)
```

### 대상 그룹
- **대상그룹 1**: EC2-1 (Coupon Service, 8081)
- **대상그룹 2**: EC2-2 (General Service, 8080)

---

## ✨ 장점

1. **단순함**: Gradle 의존성으로 자동 통합
2. **안정성**: Bean 충돌 없음
3. **EC2 최적화**: 2개 EC2 구조에 완벽 대응
4. **유지보수**: 명확한 서비스 분리
5. **확장성**: 필요 시 서비스 분리 가능

---

## 🔄 개발 워크플로우

### General Service 수정
```
1. user-service, product-service, order-service에서 코드 수정
2. general-service는 Gradle 의존성으로 자동 포함
3. GeneralServiceApplication 재시작
```

### Coupon Service 수정
```
1. coupon-service에서 코드 수정
2. CouponServiceApplication 재시작
```

---

## 📚 관련 문서

- [README.md](README.md) - 프로젝트 개요
- [QUICK_START.md](QUICK_START.md) - 빠른 시작
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - API 명세
- [deployment/SIMPLE_DEPLOY_GUIDE.md](deployment/SIMPLE_DEPLOY_GUIDE.md) - 배포 가이드
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - 빌드 가이드
- [PORT_GUIDE.md](PORT_GUIDE.md) - 포트 설정

---

## 🎉 완료!

이제 2개의 서비스로 깔끔하게 정리된 MSA 프로젝트가 완성되었습니다.
