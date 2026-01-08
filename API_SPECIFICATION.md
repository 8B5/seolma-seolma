# E-Commerce MSA API 명세서

## 📋 개요

이 문서는 E-Commerce MSA 시스템의 모든 API 엔드포인트에 대한 상세한 명세를 제공합니다.

### 🏗️ 서비스 구성
- **User Service** (포트 8080): 회원 관리 및 인증
- **Product Service** (포트 8081): 상품 관리
- **Coupon Service** (포트 8082): 쿠폰 발급 및 관리
- **Order Service** (포트 8083): 주문 및 결제

### 🔐 인증 방식
- **JWT Bearer Token**: `Authorization: Bearer {token}`
- **권한 구분**: USER, ADMIN
- **자동 사용자 ID 추출**: JWT 토큰에서 자동으로 사용자 ID를 추출하여 사용
- **관리자 권한**: `@AdminOnly` 어노테이션을 통한 자동 권한 검증

### 🌐 Swagger UI 접속
각 서비스별 API 문서는 다음 URL에서 확인할 수 있습니다:
- **User Service**: http://localhost:8080/swagger-ui/index.html
- **Product Service**: http://localhost:8081/swagger-ui/index.html  
- **Coupon Service**: http://localhost:8082/swagger-ui/index.html
- **Order Service**: http://localhost:8083/swagger-ui/index.html

### 📊 공통 응답 형식
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {},
  "timestamp": "2025-01-07T10:30:00"
}
```

### 🚨 에러 코드

#### 공통 에러 코드
- **C0000**: 성공
- **C0001**: 잘못된 요청 파라미터
- **C0002**: 인증 실패
- **C0003**: 권한 없음
- **C0004**: 리소스를 찾을 수 없음
- **C0005**: 서버 내부 오류
- **C0006**: 데이터베이스 오류

#### 사용자 서비스 에러 코드
- **L0001**: 중복된 로그인 아이디
- **L0002**: 잘못된 로그인 정보
- **L0003**: 사용자를 찾을 수 없음

#### 상품 서비스 에러 코드
- **P0001**: 상품을 찾을 수 없음
- **P0002**: 이미 삭제된 상품
- **P0003**: 상품 이미지 업로드 실패

#### 쿠폰 서비스 에러 코드
- **CP0001**: 쿠폰 템플릿을 찾을 수 없음
- **CP0002**: 이미 발급된 쿠폰
- **CP0003**: 발급 기간이 아님
- **CP0004**: 쿠폰을 찾을 수 없음
- **CP0005**: 이미 사용된 쿠폰
- **CP0006**: 사용할 수 없는 쿠폰
- **CP1004**: 이미 발급받은 쿠폰
- **CP1008**: 발급된 쿠폰이 있어 삭제할 수 없음
- **CP1009**: 쿠폰이 모두 소진됨

#### 주문 서비스 에러 코드
- **O0001**: 주문을 찾을 수 없음
- **O0002**: 이미 취소된 주문
- **O0003**: 취소할 수 없는 주문 상태
- **O0004**: 결제 처리 실패

### 📋 검증 에러 메시지

API 요청 시 필드 검증에 실패하면 해당 필드의 구체적인 에러 메시지가 응답됩니다:

**예시 - 회원가입 검증 실패:**
```json
{
  "code": "C0001",
  "message": "비밀번호는 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다",
  "data": null,
  "timestamp": "2025-01-07T10:30:00"
}
```

**주요 검증 메시지:**
- 로그인 아이디: "로그인 아이디는 영문, 숫자, 언더스코어만 사용 가능합니다"
- 비밀번호: "비밀번호는 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다"
- 사용자 이름: "사용자 이름은 2자 이상 50자 이하여야 합니다"

---

## 🔑 1. User Service (포트 8080)

### 1.1 인증 API

#### 회원가입
- **POST** `/api/v1/users/auth/signup`
- **설명**: 새로운 사용자를 등록합니다 (일반 사용자 또는 관리자)
- **인증**: 불필요

**요청 본문 (일반 사용자):**
```json
{
  "loginId": "user123",
  "password": "Password123!",
  "userName": "홍길동"
}
```

**요청 본문 (관리자):**
```json
{
  "loginId": "admin123",
  "password": "AdminPass123!",
  "userName": "관리자",
  "role": "ADMIN"
}
```

**필드 검증 규칙:**
- `loginId`: 3-50자, 영문/숫자/언더스코어만 허용
- `password`: 8-100자, 대소문자/숫자/특수문자 각각 하나 이상 포함
- `userName`: 2-50자
- `role`: USER (기본값) 또는 ADMIN

**성공 응답:**
```json
{
  "code": "C0000",
  "message": "회원가입이 완료되었습니다",
  "data": null,
  "timestamp": "2025-01-07T10:30:00"
}
```

**검증 실패 응답:**
```json
{
  "code": "C0001",
  "message": "비밀번호는 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다",
  "data": null,
  "timestamp": "2025-01-07T10:30:00"
}
```

**에러 코드:**
- `L0001`: 중복된 로그인 아이디

#### 로그인 아이디 중복확인
- **GET** `/api/v1/users/auth/check-loginid`
- **설명**: 회원가입 시 로그인 아이디의 사용 가능 여부를 확인합니다
- **인증**: 불필요

**쿼리 파라미터:**
- `loginId`: 확인할 로그인 아이디 (필수)

**응답 (사용 가능한 경우):**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {
    "available": true,
    "loginId": "user123",
    "message": "사용 가능한 아이디입니다"
  },
  "timestamp": "2025-01-08T10:30:00"
}
```

**응답 (이미 사용 중인 경우):**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {
    "available": false,
    "loginId": "admin",
    "message": "이미 사용 중인 아이디입니다"
  },
  "timestamp": "2025-01-08T10:30:00"
}
```

#### 로그인
- **POST** `/api/v1/users/auth/login`
- **설명**: 사용자 로그인을 처리하고 JWT 토큰을 발급합니다
- **인증**: 불필요

**요청 본문:**
```json
{
  "loginId": "user123",
  "password": "Password123!"
}
```

**응답:**
```json
{
  "code": "C0000",
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": "user123",
    "userName": "홍길동",
    "role": "USER"
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

---

## 🛍️ 2. Product Service (포트 8081)

### 2.1 사용자 상품 API

#### 상품 목록 조회
- **GET** `/api/v1/products`
- **설명**: 판매 중인 상품 목록을 조회합니다
- **인증**: 불필요

**쿼리 파라미터:**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

**응답:**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {
    "content": [
      {
        "productId": 1,
        "name": "스마트폰",
        "sellerId": "admin",
        "price": 999000.00,
        "isDeleted": false,
        "createdAt": "2025-01-07T10:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

#### 상품 상세 조회
- **GET** `/api/v1/products/{productId}`
- **설명**: 특정 상품의 상세 정보를 조회합니다
- **인증**: 불필요

**응답:**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {
    "productId": 1,
    "name": "스마트폰",
    "sellerId": "admin",
    "price": 999000.00,
    "isDeleted": false,
    "createdAt": "2025-01-07T10:00:00"
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

#### 상품 검색
- **GET** `/api/v1/products/search`
- **설명**: 다양한 조건으로 상품을 검색합니다
- **인증**: 불필요

**쿼리 파라미터:**
- `keyword`: 검색 키워드
- `sellerId`: 판매자 ID
- `minPrice`: 최소 가격
- `maxPrice`: 최대 가격
- `page`: 페이지 번호
- `size`: 페이지 크기

### 2.2 관리자 상품 API

#### 상품 등록
- **POST** `/api/v1/admin/products`
- **설명**: 관리자가 새로운 상품을 등록합니다
- **인증**: 필요 (ADMIN)

**요청 본문:**
```json
{
  "name": "스마트폰",
  "price": 999000.00
}
```

**응답:**
```json
{
  "code": "C0000",
  "message": "상품이 등록되었습니다",
  "data": {
    "productId": 1,
    "name": "스마트폰",
    "sellerId": "admin",
    "price": 999000.00,
    "isDeleted": false,
    "createdAt": "2025-01-07T10:30:00"
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

#### 전체 상품 목록 조회 (관리자)
- **GET** `/api/v1/admin/products`
- **설명**: 관리자가 모든 상품을 조회합니다 (삭제된 상품 포함)
- **인증**: 필요 (ADMIN)

**쿼리 파라미터:**
- `keyword`: 검색 키워드
- `sellerId`: 판매자 ID
- `minPrice`: 최소 가격
- `maxPrice`: 최대 가격
- `includeDeleted`: 삭제된 상품 포함 여부 (기본값: true)

#### 상품 수정
- **PUT** `/api/v1/admin/products/{productId}`
- **설명**: 관리자가 상품 정보를 수정합니다
- **인증**: 필요 (ADMIN)

#### 상품 삭제
- **DELETE** `/api/v1/admin/products/{productId}`
- **설명**: 관리자가 상품을 삭제합니다 (소프트 삭제)
- **인증**: 필요 (ADMIN)

#### 상품 이미지 업로드
- **POST** `/api/v1/admin/products/{productId}/images`
- **설명**: 관리자가 상품에 이미지를 업로드합니다
- **인증**: 필요 (ADMIN)
- **Content-Type**: multipart/form-data

---

## 🎫 3. Coupon Service (포트 8082)

### 3.1 사용자 쿠폰 API

#### 발급 가능한 쿠폰 목록 조회
- **GET** `/api/v1/coupons/templates/available`
- **설명**: 현재 발급 가능한 쿠폰 템플릿 목록을 조회합니다
- **인증**: 불필요

**응답:**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": [
    {
      "templateId": 1,
      "title": "신규 회원 할인 쿠폰",
      "discountType": "PERCENT",
      "discountValue": 10,
      "startedAt": "2025-01-01T00:00:00",
      "finishedAt": "2025-12-31T23:59:59"
    }
  ],
  "timestamp": "2025-01-07T10:30:00"
}
```

#### 쿠폰 발급
- **POST** `/api/v1/coupons/issue`
- **설명**: 사용자에게 쿠폰을 발급합니다 (선착순 기능 지원)
- **인증**: 필요 (USER)

**요청 본문:**
```json
{
  "templateId": 1
}
```

**성공 응답:**
```json
{
  "code": "C0000",
  "message": "쿠폰이 발급되었습니다",
  "data": {
    "couponId": 1,
    "templateId": 1,
    "userId": "user123",
    "title": "선착순 100명 할인쿠폰",
    "discountType": "FIXED",
    "discountValue": 5000,
    "isUsed": false,
    "usedAt": null,
    "issuedAt": "2025-01-09T10:30:00"
  },
  "timestamp": "2025-01-09T10:30:00"
}
```

**에러 응답 (매진된 경우):**
```json
{
  "code": "CP1009",
  "message": "쿠폰이 모두 소진되었습니다",
  "data": null,
  "timestamp": "2025-01-09T10:30:00"
}
```

**에러 응답 (중복 발급):**
```json
{
  "code": "CP1004",
  "message": "이미 발급받은 쿠폰입니다",
  "data": null,
  "timestamp": "2025-01-09T10:30:00"
}
```

#### 내 쿠폰 목록 조회
- **GET** `/api/v1/coupons/my`
- **설명**: 사용자의 모든 쿠폰 목록을 조회합니다
- **인증**: 필요 (USER)

#### 사용 가능한 내 쿠폰 조회
- **GET** `/api/v1/coupons/my/available`
- **설명**: 사용자의 사용 가능한 쿠폰 목록을 조회합니다
- **인증**: 필요 (USER)

#### 쿠폰 사용
- **PATCH** `/api/v1/coupons/{couponId}/use`
- **설명**: 쿠폰을 사용 처리합니다
- **인증**: 필요 (USER)

### 3.2 관리자 쿠폰 API

#### 쿠폰 템플릿 등록
- **POST** `/api/v1/admin/coupons/templates`
- **설명**: 관리자가 새로운 쿠폰 템플릿을 등록합니다 (선착순 기능 지원)
- **인증**: 필요 (ADMIN)

**요청 본문 (무제한 쿠폰):**
```json
{
  "title": "신규 회원 할인 쿠폰",
  "discountType": "PERCENT",
  "discountValue": 10,
  "startedAt": "2025-01-01T00:00:00",
  "finishedAt": "2025-12-31T23:59:59",
  "isLimited": false
}
```

**요청 본문 (선착순 쿠폰):**
```json
{
  "title": "선착순 100명 할인쿠폰",
  "discountType": "FIXED",
  "discountValue": 5000,
  "startedAt": "2025-01-09T00:00:00",
  "finishedAt": "2025-01-31T23:59:59",
  "isLimited": true,
  "totalQuantity": 100
}
```

**필드 설명:**
- `isLimited`: 수량 제한 여부 (기본값: false)
- `totalQuantity`: 총 발급 가능 수량 (isLimited=true일 때 필수)

**응답:**
```json
{
  "code": "C0000",
  "message": "쿠폰 템플릿이 등록되었습니다",
  "data": {
    "templateId": 1,
    "title": "선착순 100명 할인쿠폰",
    "discountType": "FIXED",
    "discountValue": 5000,
    "startedAt": "2025-01-09T00:00:00",
    "finishedAt": "2025-01-31T23:59:59",
    "isLimited": true,
    "totalQuantity": 100,
    "issuedCount": 0,
    "isSoldOut": false,
    "isAvailable": true,
    "isExpired": false
  },
  "timestamp": "2025-01-09T10:30:00"
}
```

#### 모든 쿠폰 템플릿 조회
- **GET** `/api/v1/admin/coupons/templates`
- **설명**: 관리자가 모든 쿠폰 템플릿을 조회합니다
- **인증**: 필요 (ADMIN)

#### 쿠폰 템플릿 수정
- **PUT** `/api/v1/admin/coupons/templates/{templateId}`
- **설명**: 관리자가 쿠폰 템플릿을 수정합니다
- **인증**: 필요 (ADMIN)

#### 쿠폰 템플릿 삭제
- **DELETE** `/api/v1/admin/coupons/templates/{templateId}`
- **설명**: 관리자가 쿠폰 템플릿을 삭제합니다 (소프트 삭제)
- **인증**: 필요 (ADMIN)
- **참고**: 이미 발급된 쿠폰이 있는 템플릿은 삭제할 수 없습니다

**응답:**
```json
{
  "code": "C0000",
  "message": "쿠폰 템플릿이 삭제되었습니다",
  "data": null,
  "timestamp": "2025-01-09T10:30:00"
}
```

**에러 응답 (발급된 쿠폰이 있는 경우):**
```json
{
  "code": "CP1008",
  "message": "발급된 쿠폰이 있어 삭제할 수 없습니다",
  "data": null,
  "timestamp": "2025-01-09T10:30:00"
}
```

#### 전체 쿠폰 발급 내역 조회
- **GET** `/api/v1/admin/coupons/issued`
- **설명**: 관리자가 모든 쿠폰 발급 내역을 조회합니다
- **인증**: 필요 (ADMIN)

**쿼리 파라미터:**
- `templateId`: 쿠폰 템플릿 ID
- `userId`: 사용자 ID
- `isUsed`: 사용 여부 (true/false)
- `page`: 페이지 번호
- `size`: 페이지 크기

---

## 📦 4. Order Service (포트 8083)

### 4.1 사용자 주문 API

#### 주문 생성
- **POST** `/api/v1/orders`
- **설명**: 새로운 주문을 생성하고 결제를 처리합니다
- **인증**: 필요 (USER)

**요청 본문:**
```json
{
  "ordererName": "홍길동",
  "shippingAddress": "서울시 강남구 테헤란로 123",
  "productId": 1,
  "quantity": 2,
  "appliedCouponId": 1,
  "paymentMethod": "CARD"
}
```

**응답:**
```json
{
  "code": "C0000",
  "message": "주문이 완료되었습니다",
  "data": {
    "orderId": 1,
    "userId": "user123",
    "ordererName": "홍길동",
    "shippingAddress": "서울시 강남구 테헤란로 123",
    "productId": 1,
    "productSnapshotName": "스마트폰",
    "quantity": 2,
    "totalPrice": 1798200.00,
    "appliedCouponId": 1,
    "paymentMethod": "CARD",
    "status": "PAYMENT_COMPLETED",
    "orderedAt": "2025-01-07T10:30:00",
    "cancelledAt": null
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

#### 내 주문 목록 조회
- **GET** `/api/v1/orders/my`
- **설명**: 사용자의 주문 목록을 조회합니다
- **인증**: 필요 (USER)

**쿼리 파라미터:**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

#### 주문 상세 조회
- **GET** `/api/v1/orders/{orderId}`
- **설명**: 특정 주문의 상세 정보를 조회합니다
- **인증**: 필요 (USER)

#### 주문 취소
- **PATCH** `/api/v1/orders/{orderId}/cancel`
- **설명**: 주문을 취소합니다
- **인증**: 필요 (USER)

### 4.2 관리자 주문 API

#### 전체 주문 목록 조회
- **GET** `/api/v1/admin/orders`
- **설명**: 관리자가 모든 주문을 조회합니다
- **인증**: 필요 (ADMIN)

**쿼리 파라미터:**
- `userId`: 사용자 ID
- `status`: 주문 상태 (PAYMENT_COMPLETED, SHIPPING, DELIVERED, CANCELLED)
- `productId`: 상품 ID
- `page`: 페이지 번호
- `size`: 페이지 크기

#### 주문 상세 조회 (관리자)
- **GET** `/api/v1/admin/orders/{orderId}`
- **설명**: 관리자가 특정 주문의 상세 정보를 조회합니다
- **인증**: 필요 (ADMIN)

#### 주문 상태 변경
- **PATCH** `/api/v1/admin/orders/{orderId}/status`
- **설명**: 관리자가 주문의 상태를 변경합니다
- **인증**: 필요 (ADMIN)

**쿼리 파라미터:**
- `status`: 변경할 상태 (PAYMENT_COMPLETED, SHIPPING, DELIVERED, CANCELLED)

#### 주문 통계
- **GET** `/api/v1/admin/orders/statistics`
- **설명**: 주문 관련 통계를 조회합니다
- **인증**: 필요 (ADMIN)

**쿼리 파라미터:**
- `startDate`: 시작 날짜 (yyyy-MM-dd)
- `endDate`: 종료 날짜 (yyyy-MM-dd)

**응답:**
```json
{
  "code": "C0000",
  "message": "성공",
  "data": {
    "totalOrders": 1000,
    "completedOrders": 850,
    "cancelledOrders": 50,
    "shippingOrders": 100,
    "totalRevenue": "85000000.00",
    "completionRate": 85.0
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

---

## 🔧 5. 환경 설정

### 5.1 서비스 URL 설정

각 서비스는 환경 변수를 통해 다른 서비스의 URL을 설정합니다:

```yaml
# application.yml
services:
  user-service:
    url: ${USER_SERVICE_URL:http://localhost:8080}
  product-service:
    url: ${PRODUCT_SERVICE_URL:http://localhost:8081}
  coupon-service:
    url: ${COUPON_SERVICE_URL:http://localhost:8082}
  order-service:
    url: ${ORDER_SERVICE_URL:http://localhost:8083}
```

### 5.2 데이터베이스 설정

- **common_db**: users, products, product_images, orders 테이블
- **coupon_db**: coupon_templates, coupons 테이블

### 5.3 JWT 설정

JWT 토큰은 다음 정보를 포함합니다:
- `sub`: 사용자 ID
- `role`: 사용자 권한 (USER, ADMIN)
- `exp`: 만료 시간

### 5.4 환경 변수

시스템에서 사용하는 주요 환경 변수:
- `ADMIN_SECRET_KEY`: 관리자 등록 활성화 여부 확인용 (설정되어 있으면 관리자 등록 허용)
- `JWT_SECRET`: JWT 토큰 서명용 비밀 키
- `USER_SERVICE_URL`: 사용자 서비스 URL
- `PRODUCT_SERVICE_URL`: 상품 서비스 URL  
- `COUPON_SERVICE_URL`: 쿠폰 서비스 URL
- `ORDER_SERVICE_URL`: 주문 서비스 URL

---

## 📝 6. 주요 특징

### 6.1 보안
- JWT 기반 인증/인가
- `@AdminOnly` 어노테이션을 통한 관리자 권한 검증
- 자동 사용자 ID 추출 (SecurityUtils.getCurrentUserId())
- 환경 변수를 통한 관리자 등록 제어 (ADMIN_SECRET_KEY 설정 시에만 관리자 등록 허용)
- 비밀번호 암호화 (BCrypt)

### 6.2 확장성
- MSA 구조로 서비스별 독립적 확장 가능
- 환경 변수를 통한 서비스 URL 설정
- 페이징 처리를 통한 대용량 데이터 처리
- 서비스별 독립적인 데이터베이스

### 6.3 데이터 일관성
- 서비스 간 통신을 위한 전용 클라이언트 클래스
- 트랜잭션 처리 및 롤백 지원
- 소프트 삭제를 통한 데이터 보존
- 주문 시 상품 정보 스냅샷 저장

### 6.4 모니터링 및 관리
- 통계 API를 통한 비즈니스 메트릭 제공
- 상세한 로깅 및 에러 추적
- 관리자 전용 모니터링 API
- Swagger UI를 통한 API 문서화

### 6.5 쿠폰 시스템
- 선착순 쿠폰 발급 기능 (수량 제한)
- 동시성 제어를 통한 안전한 쿠폰 발급
- 쿠폰 템플릿 소프트 삭제 (발급된 쿠폰 보호)
- 실시간 매진 상태 확인
- 중복 발급 방지

### 6.6 사용자 경험
- 구체적인 검증 에러 메시지 제공
- 일관된 API 응답 형식
- 페이징을 통한 효율적인 데이터 조회
- 실시간 쿠폰 발급 및 사용
- 파라미터 검증 및 자동 정정 (sort 필드 등)

---

## 🚀 7. 배포 정보

### 7.1 ALB 라우팅 규칙
1. **Priority 1**: `/api/v1/coupons/**` → Coupon Service (포트 8082)
2. **Default**: `/**` → 기타 모든 요청 → 각 서비스별 포트

### 7.2 서비스 포트
- User Service: 8080
- Product Service: 8081  
- Coupon Service: 8082
- Order Service: 8083

### 7.3 배포 형태
- WAR 파일로 빌드
- Standalone Tomcat 10에서 실행
- Nginx를 통한 리버스 프록시

---

## 📚 8. 추가 정보

### 8.1 개발 환경 설정
```bash
# 각 서비스 빌드
./gradlew build

# 서비스별 실행 (개발 환경)
java -jar user-service/build/libs/user-service.war --server.port=8080
java -jar product-service/build/libs/product-service.war --server.port=8081
java -jar coupon-service/build/libs/coupon-service.war --server.port=8082
java -jar order-service/build/libs/order-service.war --server.port=8083
```

### 8.2 데이터베이스 스키마
- **common_db**: 
  - users (user_id VARCHAR(50) PK, password, user_name, role, created_at)
  - products (product_id BIGINT PK, name, seller_id VARCHAR(50), price, is_deleted, created_at)
  - product_images (image_id BIGINT PK, product_id, image_url, is_deleted, created_at)
  - orders (order_id BIGINT PK, user_id VARCHAR(50), orderer_name, shipping_address, product_id, product_snapshot_name, quantity, total_price, applied_coupon_id, payment_method, status, ordered_at, cancelled_at)

- **coupon_db**:
  - coupon_templates (template_id BIGINT PK, title, discount_type, discount_value, started_at, finished_at, is_limited, total_quantity, is_deleted)
  - coupons (coupon_id BIGINT PK, template_id, user_id VARCHAR(50), is_used, used_at, issued_at)

### 8.3 보안 고려사항
- 모든 비밀번호는 BCrypt로 암호화
- JWT 토큰은 HTTPS 환경에서만 사용 권장
- 관리자 등록은 환경 변수 설정을 통해 제어 (운영환경에서는 ADMIN_SECRET_KEY 미설정 권장)
- API 엔드포인트별 적절한 권한 검증

### 8.4 성능 최적화
- 데이터베이스 인덱스 설정 (user_id, product_id 등)
- 페이징을 통한 메모리 효율성
- 서비스 간 통신 최적화 (WebClient 사용)
- 불필요한 데이터 조회 방지

---

이 API 명세서는 E-Commerce MSA 시스템의 모든 엔드포인트를 포함하며, 각 API의 요청/응답 형식, 인증 요구사항, 검증 규칙, 그리고 주요 비즈니스 로직을 상세히 설명합니다. 시스템은 확장 가능한 MSA 구조로 설계되어 있으며, 보안과 사용자 경험을 모두 고려한 설계가 적용되어 있습니다.