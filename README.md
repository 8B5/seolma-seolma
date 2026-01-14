# E-Commerce MSA Project

🛒 MSA 기반의 확장 가능한 이커머스 시스템입니다.

## 🚀 빠른 시작

### 로컬 개발 환경

```cmd
REM General Service 실행 (User + Product + Order 통합)
gradlew :general-service:bootRun

REM Coupon Service 실행
gradlew :coupon-service:bootRun
```

- General Service: http://localhost:8080/swagger-ui.html
- Coupon Service: http://localhost:8081/swagger-ui.html

자세한 내용: [QUICK_START.md](QUICK_START.md)

## 🏗️ 아키텍처

### 시스템 구조
- **아키텍처**: MSA (Microservices Architecture)
- **배포 방식**: WAR 형태로 Tomcat 10에 배포
- **데이터베이스**: MariaDB (common_db, coupon_db)

### 서비스 구성

**개발 환경:**
- **General Service** (포트 8080): User + Product + Order 통합 → `common_db`
- **Coupon Service** (포트 8081): 쿠폰 발급 및 관리 → `coupon_db`

**운영 환경:**
- **EC2-1** (8081): Coupon Service
- **EC2-2** (8080): General Service (User + Product + Order)

### 데이터베이스 구성
- **common_db**: 사용자, 상품, 주문 정보
- **coupon_db**: 쿠폰 템플릿 및 발급 내역

### 서비스 간 통신
- 환경 변수를 통한 서비스 URL 설정
- WebClient를 사용한 비동기 HTTP 통신
- Order Service → Coupon Service 호출 (쿠폰 검증)

## 🛠️ 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.x, Spring Security
- **Database**: MariaDB, JPA (Hibernate), QueryDSL
- **Security**: JWT (AccessToken), BCrypt
- **Build**: Gradle 8.5
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Test**: JUnit 5, AssertJ, Mockito
- **File Storage**: Local File System / AWS S3
- **Deployment**: Tomcat 10 (WAR)

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
├── general-service/           # 통합 서비스 (포트 8080)
│   ├── src/main/java/com/ecommerce/general/
│   │   └── GeneralServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── build.gradle          # user/product/order 의존성 포함
├── user-service/              # 회원 서비스 (소스 코드)
│   ├── domain/               # User 엔티티
│   ├── repository/           # 데이터 접근
│   ├── service/              # 비즈니스 로직
│   ├── controller/           # API 컨트롤러
│   └── config/               # 보안, OpenAPI 설정
├── product-service/           # 상품 서비스 (소스 코드)
│   ├── domain/               # Product, ProductImage 엔티티
│   ├── service/              # 파일 저장소 추상화
│   ├── controller/           # 상품 조회, 관리자 CRUD
│   └── config/               # WebConfig (이미지 서빙)
├── order-service/             # 주문 서비스 (소스 코드)
│   ├── domain/               # Order 엔티티
│   ├── client/               # 외부 서비스 통신
│   ├── service/              # 주문 처리, 통계
│   └── controller/           # 사용자 주문, 관리자 관리
├── coupon-service/            # 쿠폰 서비스 (포트 8081)
│   ├── domain/               # CouponTemplate, Coupon 엔티티
│   ├── service/              # 선착순 발급, 동시성 제어
│   └── controller/           # 사용자 발급, 관리자 관리
├── deployment/               # 배포 관련
│   ├── SIMPLE_DEPLOY_GUIDE.md  # 배포 가이드
│   ├── nginx.conf           # Nginx 설정
│   ├── tomcat-setup.sh      # Tomcat 설치 스크립트
│   └── env-examples/        # 환경 변수 예시
├── build-general.bat         # General Service 빌드
├── build-coupon.bat          # Coupon Service 빌드
└── README.md
```

**중요:** general-service는 Gradle 의존성을 통해 user/product/order 서비스를 자동으로 통합합니다.

## 🚀 주요 기능

### 🔐 인증 및 권한 관리
- **JWT 토큰 시스템**: AccessToken (1-8시간)
- 역할 기반 접근 제어 (USER, ADMIN)
- 자동 사용자 ID 추출 (SecurityUtils.getCurrentUserId())
- @AdminOnly 어노테이션을 통한 관리자 권한 자동 검증
- 로그인 아이디 중복 확인 API

### 🛍️ 상품 관리
- 관리자 전용 상품 CRUD 기능
- 다중 이미지 업로드 (multipart/form-data)
- 파일 저장소 추상화 (Local / S3)
- 상품 검색 및 필터링 (QueryDSL)
- 소프트 삭제 (상품 삭제 시 이미지도 연쇄 삭제)
- 이미지 URL 자동 포함 (N+1 방지)

### 🎫 쿠폰 시스템
- **선착순 쿠폰**: 수량 제한 및 동시성 제어
- **무제한 쿠폰**: 기간 내 무제한 발급
- 쿠폰 템플릿 소프트 삭제 (발급된 쿠폰 보호)
- 중복 발급 방지
- 실시간 매진 상태 확인
- 쿠폰 사용 처리 및 검증

### 📦 주문 처리
- 상품 정보 스냅샷 저장
- 쿠폰 적용 및 할인 계산 (실제 API 호출)
- 주문 상태 관리 (결제완료, 배송중, 배송완료, 취소)
- 서비스 간 통신을 통한 상품/쿠폰 검증
- 주문 통계 및 관리자 모니터링

### 🔧 시스템 안정성
- 글로벌 예외 처리 및 구체적인 에러 메시지
- 파라미터 검증 및 자동 정정
- 서비스 간 통신 에러 핸들링
- 트랜잭션 처리 및 롤백 지원
- JPA Auditing (자동 생성일시 관리)

## 🚀 빠른 시작

### 1. 로컬 개발 환경 실행

```cmd
REM General Service 실행
gradlew :general-service:bootRun

REM Coupon Service 실행 (별도 터미널)
gradlew :coupon-service:bootRun
```

### 2. WAR 파일 빌드 (배포용)

```cmd
REM General Service 빌드
build-general.bat

REM Coupon Service 빌드
build-coupon.bat
```

빌드 결과:
- `general-service/build/libs/general-service-1.0.0.war`
- `coupon-service/build/libs/coupon-service-1.0.0.war`

### 3. EC2 배포

자세한 배포 방법은 [deployment/SIMPLE_DEPLOY_GUIDE.md](deployment/SIMPLE_DEPLOY_GUIDE.md) 참고

## 🔧 환경 설정

### 필수 환경 변수

```bash
# 데이터베이스
DB_HOST=localhost
DB_PORT=3306
DB_NAME=common_db  # General Service
DB_USERNAME=dev_user
DB_PASSWORD=dev_password

# JWT
JWT_SECRET=your-secret-key-at-least-256-bits-long
JWT_VALIDITY=3600

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
CORS_ALLOWED_CREDENTIALS=true

# 서비스 간 통신
COUPON_SERVICE_URL=http://localhost:8081
PRODUCT_SERVICE_URL=http://localhost:8080
USER_SERVICE_URL=http://localhost:8080

# 파일 저장소
FILE_STORAGE_TYPE=local
FILE_UPLOAD_DIR=./uploads
```

## 📊 API 문서

서비스 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다:
- General Service: http://localhost:8080/swagger-ui.html
- Coupon Service: http://localhost:8081/swagger-ui.html

자세한 API 명세: [API_SPECIFICATION.md](API_SPECIFICATION.md)

## 🧪 테스트

```bash
# 전체 테스트 실행
gradlew test

# 특정 서비스 테스트
gradlew :product-service:test
gradlew :coupon-service:test
```

## 🚀 배포

### 개발환경 (로컬)
```cmd
gradlew :general-service:bootRun
gradlew :coupon-service:bootRun
```

### 운영환경 (EC2 + Tomcat)

#### EC2-1 (Coupon Service - 8081)
```bash
# WAR 파일 배포
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/ROOT*
sudo cp coupon-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
```

#### EC2-2 (General Service - 8080)
```bash
# WAR 파일 배포
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/ROOT*
sudo cp general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
```

자세한 배포 가이드: [deployment/SIMPLE_DEPLOY_GUIDE.md](deployment/SIMPLE_DEPLOY_GUIDE.md)

## 🔍 모니터링

### Health Check
- General Service: http://localhost:8080/actuator/health
- Coupon Service: http://localhost:8081/actuator/health

### 로그 확인
```bash
# Tomcat 로그
sudo tail -f /opt/tomcat/logs/catalina.out

# Systemd 로그
sudo journalctl -u tomcat -f
```

## 🔐 보안 고려사항

1. **JWT 토큰**: 
   - AccessToken 유효기간 (1-8시간)
   - 안전한 Secret Key 사용 (모든 서비스 공유)
   - 만료된 토큰: 401 Unauthorized 응답

2. **비밀번호**: BCrypt 암호화

3. **환경 변수**: 민감 정보는 환경 변수로 관리

4. **HTTPS**: 운영 환경에서 HTTPS 필수

5. **데이터 마스킹**: 개인정보 로깅 시 마스킹 처리

6. **권한 검증**: @AdminOnly 어노테이션을 통한 자동 권한 검증

7. **Internal API**: `/api/v1/internal/**` 경로는 인증 불필요 (서비스 간 통신용)

## 📈 성능 최적화

1. **JPA**: 
   - FetchType.LAZY 사용
   - @EntityGraph로 N+1 문제 방지
   - QueryDSL fetch join 활용

2. **Connection Pool**: HikariCP 최적화

3. **Database**: 인덱스 최적화 (user_id, product_id 등)

4. **동시성 제어**: 선착순 쿠폰 발급 시 synchronized 블록 사용

5. **소프트 삭제**: 데이터 보존 및 성능 최적화

6. **이미지 서빙**: Static Resource Handler로 효율적 처리

## 🔧 문제 해결

### JWT 토큰 관련
```bash
# AccessToken 만료 (401 Unauthorized)
# → 재로그인 필요

# 토큰 검증 실패
# → JWT_SECRET 환경변수 확인 (모든 서비스 동일해야 함)
```

### 서비스 간 통신 실패
```bash
# Order Service → Coupon Service 호출 실패
# → COUPON_SERVICE_URL 환경변수 확인
# → Coupon Service 실행 상태 확인
# → 보안 그룹 포트 개방 확인 (EC2)
```

### 이미지 접근 403 에러
```bash
# /images/** 경로가 permitAll 설정되어 있는지 확인
# WebConfig에서 Resource Handler 설정 확인
```

### 빌드 실패
```bash
# Gradle Wrapper 문제
# → IDE에서 직접 빌드 (BUILD_INSTRUCTIONS.md 참고)
```

## 📚 추가 문서

- [QUICK_START.md](QUICK_START.md) - 빠른 시작 가이드
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - API 명세서
- [deployment/SIMPLE_DEPLOY_GUIDE.md](deployment/SIMPLE_DEPLOY_GUIDE.md) - 배포 가이드
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - 빌드 가이드
- [PORT_GUIDE.md](PORT_GUIDE.md) - 포트 설정 가이드

## 🤝 기여 가이드

1. 코드 스타일: Google Java Style Guide 준수
2. 테스트: 모든 비즈니스 로직에 대한 단위 테스트 필수
3. API 문서: SpringDoc 어노테이션으로 문서화
4. 커밋 메시지: Conventional Commits 규칙 준수

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.
