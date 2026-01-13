# E-Commerce MSA Project

🐙 MSA 기반의 확장 가능한 이커머스 시스템입니다.

## 🏗️ 아키텍처

### 시스템 구조
- **아키텍처**: MSA (Microservices Architecture)
- **인프라**: AWS 3-Tier (Nginx - Tomcat - RDS)
- **네트워크**: PRD/DEV VPC 완전 분리
- **배포**: WAR 형태로 Standalone Tomcat 10에 배포

### 서비스 구성
- **User Service** (포트 8080): 회원 관리 및 인증 → `common_db`
- **Product Service** (포트 8081): 상품 관리 → `common_db`
- **Coupon Service** (포트 8082): 쿠폰 발급 및 관리 → `coupon_db`
- **Order Service** (포트 8083): 주문 및 결제 → `common_db`

### 배포 구조 (3-Tier)
- **Tier 1**: Nginx (Load Balancer/API Gateway)
- **Tier 2**: Tomcat (Application Servers)
- **Tier 3**: MariaDB (Database)

**개발환경**: 단일 서버에서 모든 서비스 실행
**운영환경**: 서비스별 독립 배포
  - **EC2-1**: User + Product + Order (일반 서비스)
  - **EC2-2**: Coupon 전용 (선착순 트래픽)

### 데이터베이스 구성
- **common_db**: 사용자, 상품, 주문 정보
- **coupon_db**: 쿠폰 템플릿 및 발급 내역

### 최근 스키마 변경사항
- **coupon_templates**: `is_limited`, `total_quantity`, `is_deleted` 필드 추가
- **product_images**: `created_at` 필드 추가
- **모든 user_id 필드**: VARCHAR(50)로 통일

### 서비스 간 통신
- 환경 변수를 통한 서비스 URL 설정
- WebClient를 사용한 비동기 HTTP 통신
- 각 서비스별 전용 클라이언트 클래스

## 🛠️ 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.x, Spring Security
- **Database**: MariaDB, JPA (Hibernate)
- **Security**: JWT (단일 토큰), BCrypt
- **Build**: Gradle
- **Documentation**: SpringDoc OpenAPI
- **Test**: JUnit 5, AssertJ, Mockito
- **File Storage**: Local File System (S3 마이그레이션 준비)
- **Deployment**: 2-Tier MSA (Heavy/General Services)

## 📁 프로젝트 구조

```
ecommerce-msa/
├── common-lib/                 # 공통 라이브러리
│   ├── response/              # 공통 응답 구조
│   ├── exception/             # 글로벌 예외 처리
│   ├── security/              # JWT 토큰 관리, @AdminOnly
│   ├── annotation/            # 커스텀 어노테이션
│   ├── aspect/                # AOP (권한 검증)
│   └── util/                  # 유틸리티 (마스킹 등)
├── user-service/              # 회원 서비스 (포트 8080)
│   ├── domain/               # User 엔티티
│   ├── repository/           # 데이터 접근
│   ├── service/              # 비즈니스 로직
│   ├── controller/           # API 컨트롤러 (Auth, Admin)
│   ├── dto/                  # 요청/응답 DTO
│   └── config/               # 보안, OpenAPI 설정
├── product-service/           # 상품 서비스 (포트 8081)
│   ├── domain/               # Product, ProductImage 엔티티
│   ├── service/              # 파일 저장소 추상화
│   ├── controller/           # 상품 조회, 관리자 CRUD
│   └── client/               # 내부 API (주문 서비스용)
├── coupon-service/            # 쿠폰 서비스 (포트 8082)
│   ├── domain/               # CouponTemplate, Coupon 엔티티
│   ├── service/              # 선착순 발급, 동시성 제어
│   └── controller/           # 사용자 발급, 관리자 관리
├── order-service/             # 주문 서비스 (포트 8083)
│   ├── domain/               # Order 엔티티
│   ├── client/               # 외부 서비스 통신
│   ├── service/              # 주문 처리, 통계
│   └── controller/           # 사용자 주문, 관리자 관리
├── deployment/               # 배포 관련
│   ├── nginx.conf           # Nginx 설정
│   ├── tomcat-setup.sh      # Tomcat 설치 스크립트
│   ├── deploy.sh            # 배포 스크립트
│   └── env-examples/        # 환경 변수 예시
└── README.md
```

## 🚀 주요 기능

### 🔐 인증 및 권한 관리
- **단일 토큰 시스템**: JWT AccessToken (1-8시간)
- 역할 기반 접근 제어 (USER, ADMIN)
- 자동 사용자 ID 추출 (SecurityUtils.getCurrentUserId())
- @AdminOnly 어노테이션을 통한 관리자 권한 자동 검증
- 로그인 아이디 중복 확인 API
- 토큰 갱신 및 로그아웃 기능

### 🛍️ 상품 관리
- 관리자 전용 상품 CRUD 기능
- 다중 이미지 업로드 (multipart/form-data)
- 파일 저장소 추상화 (Local → S3 마이그레이션 준비)
- 상품 검색 및 필터링
- 소프트 삭제 (상품 삭제 시 이미지도 연쇄 삭제)

### 🎫 쿠폰 시스템
- **선착순 쿠폰**: 수량 제한 및 동시성 제어
- **무제한 쿠폰**: 기간 내 무제한 발급
- 쿠폰 템플릿 소프트 삭제 (발급된 쿠폰 보호)
- 중복 발급 방지
- 실시간 매진 상태 확인
- 쿠폰 사용 처리 및 검증

### 📦 주문 처리
- 상품 정보 스냅샷 저장
- 쿠폰 적용 및 할인 계산
- 주문 상태 관리 (결제완료, 배송중, 배송완료, 취소)
- 서비스 간 통신을 통한 상품/쿠폰 검증
- 주문 통계 및 관리자 모니터링

### 🔧 시스템 안정성
- 글로벌 예외 처리 및 구체적인 에러 메시지
- 파라미터 검증 및 자동 정정 (sort 필드 등)
- 서비스 간 통신 에러 핸들링
- 트랜잭션 처리 및 롤백 지원

## 🚀 빠른 시작

### 1. 프로젝트 빌드
```bash
./gradlew clean build
```

### 2. 개발 환경 실행
```bash
# User Service 실행
cd user-service
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. WAR 파일 빌드 (운영 배포용)
```bash
./gradlew clean bootWar
```

## 🔧 환경 설정

### 환경별 설정 파일
- `application.yml`: 기본 설정
- `application-dev.yml`: 개발 환경
- `application-prd.yml`: 운영 환경

### 필수 환경 변수
```bash
# 공통 데이터베이스 (User, Product, Order Service)
DB_HOST=localhost
DB_PORT=3306
COMMON_DB_NAME=common_db
COMMON_DB_USERNAME=dev_user
COMMON_DB_PASSWORD=dev_password

# 쿠폰 데이터베이스 (Coupon Service)
COUPON_DB_NAME=coupon_db
COUPON_DB_USERNAME=dev_user
COUPON_DB_PASSWORD=dev_password

# JWT (이중 토큰 시스템)
JWT_SECRET=your-secret-key-here
JWT_VALIDITY=3600  # JWT 토큰 유효기간 (초)

# CORS 설정
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
CORS_ALLOWED_CREDENTIALS=true

# 서비스 간 통신 URL
PRODUCT_SERVICE_URL=http://localhost:8081
COUPON_SERVICE_URL=http://localhost:8082
USER_SERVICE_URL=http://localhost:8080
ORDER_SERVICE_URL=http://localhost:8083

# 관리자 등록 제어 (설정 시에만 관리자 등록 허용)
ADMIN_SECRET_KEY=your-admin-secret-key

# 파일 저장소 설정
FILE_STORAGE_TYPE=local
FILE_STORAGE_BASE_PATH=/tmp/uploads
```

## 📊 API 문서

서비스 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- User Service: http://localhost:8080/swagger-ui.html
- Product Service: http://localhost:8081/swagger-ui.html
- Coupon Service: http://localhost:8082/swagger-ui.html
- Order Service: http://localhost:8083/swagger-ui.html

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 서비스 테스트
./gradlew :user-service:test
```

## 🚀 배포

### 1. 개발환경 (단일 서버)
```bash
# 모든 서비스를 localhost에서 실행
./gradlew :user-service:bootRun &
./gradlew :product-service:bootRun &
./gradlew :coupon-service:bootRun &
./gradlew :order-service:bootRun &
```

### 2. 운영환경 (3-Tier 구조)

#### EC2-1 (General Services)
```bash
# User Service + Product Service + Order Service
cd deployment
chmod +x tomcat-setup.sh
sudo ./tomcat-setup.sh

# 서비스 배포
./deploy.sh user-service prd
./deploy.sh product-service prd
./deploy.sh order-service prd
```

#### EC2-2 (Coupon Dedicated Server)
```bash
# Coupon Service 전용 (선착순 트래픽 테스트용)
cd deployment
chmod +x tomcat-setup.sh
sudo ./tomcat-setup.sh

# 쿠폰 서비스만 배포
./deploy.sh coupon-service prd
```

### 3. Nginx 설정 (API Gateway)
```bash
# 3-Tier 구조용 Nginx 설정 적용
sudo cp deployment/nginx.conf /etc/nginx/sites-available/ecommerce
sudo ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/
sudo systemctl reload nginx
```

## 🔍 모니터링

### Health Check
- User Service: http://localhost:8080/actuator/health
- Product Service: http://localhost:8081/actuator/health
- Coupon Service: http://localhost:8082/actuator/health
- Order Service: http://localhost:8083/actuator/health

### 로그 확인
```bash
# Tomcat 로그
sudo journalctl -u tomcat-user -f
sudo journalctl -u tomcat-coupon -f

# Nginx 로그
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## 🔐 보안 고려사항

1. **JWT 토큰 시스템**: 
   - **AccessToken**: 유효기간 (1-8시간), API 인증용
   - 안전한 Secret Key 사용 (모든 서비스 공유)
   - 만료된 토큰: 401 Unauthorized 응답
2. **토큰 보안**:
   - Stateless JWT 토큰 사용
   - 토큰 만료 시 재로그인 필요
3. **비밀번호**: BCrypt 암호화
4. **환경 변수**: 민감 정보는 환경 변수로 관리
5. **HTTPS**: 운영 환경에서 HTTPS 필수
6. **데이터 마스킹**: 개인정보 로깅 시 마스킹 처리
7. **관리자 등록**: ADMIN_SECRET_KEY 환경 변수로 제어
8. **권한 검증**: @AdminOnly 어노테이션을 통한 자동 권한 검증

## 📈 성능 최적화

1. **JPA**: FetchType.LAZY 사용, N+1 문제 방지
2. **Connection Pool**: HikariCP 최적화
3. **Caching**: 필요 시 Redis 캐시 적용 예정
4. **Database**: 인덱스 최적화 (user_id, product_id 등)
5. **동시성 제어**: 선착순 쿠폰 발급 시 synchronized 블록 사용
6. **파라미터 검증**: 잘못된 sort 파라미터 자동 정정
7. **소프트 삭제**: 데이터 보존 및 성능 최적화

## 🔧 문제 해결

### 일반적인 문제들

#### JWT 토큰 관련
```bash
# AccessToken 만료 (401 Unauthorized)
# → 재로그인 필요

# 토큰 검증 실패
# → JWT_SECRET 환경변수 확인 (모든 서비스 동일해야 함)
```

#### 3-Tier 배포 문제
```bash
# 서비스 간 통신 실패
# → 환경변수 SERVICE_URL 확인
# → EC2 보안그룹 포트 개방 확인

# 쿠폰 서버 독립성 확인
# → EC2-2에서 쿠폰 서비스만 실행 중인지 확인
# → 선착순 발급 시 EC2-2 리소스 모니터링

# 토큰이 다른 서버에서 인식 안됨
# → JWT_SECRET이 모든 서비스에서 동일한지 확인
```

#### Sort 파라미터 에러
```bash
# 잘못된 sort 파라미터 사용 시 자동으로 기본값으로 정정됨
# 예: sort=string → sort=createdAt (기본값)
```

#### 쿠폰 발급 실패
```bash
# CP1009: 쿠폰이 모두 소진됨 (선착순 쿠폰)
# CP1004: 이미 발급받은 쿠폰 (중복 발급 방지)
# CP0003: 발급 기간이 아님
```

#### 파일 업로드 문제
```bash
# 파일 저장 경로 확인: /tmp/uploads/dev/products/
# Content-Type: multipart/form-data 확인
# Swagger UI에서 파일 업로드 테스트 가능
```

## 🤝 기여 가이드

1. 코드 스타일: Google Java Style Guide 준수
2. 테스트: 모든 비즈니스 로직에 대한 단위 테스트 필수
3. API 문서: SpringDoc 어노테이션으로 문서화
4. 커밋 메시지: Conventional Commits 규칙 준수