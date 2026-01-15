# 포트 설정 가이드

## 최종 포트 구조

### 개발 환경 (로컬)

```
localhost:8080 - General Service (User + Product + Order 통합)
localhost:8081 - Coupon Service
```

### 운영 환경 (AWS)

```
EC2-1 (10.100.2.100:8081) - Coupon Service
EC2-2 (10.100.2.200:8080) - General Service (User + Product + Order 통합)
```

---

## 개발 환경 실행 방법

### 방법 1: Gradle bootRun (권장)

**터미널 1 - General Service 실행:**
```cmd
gradlew :general-service:bootRun
```

**터미널 2 - Coupon Service 실행:**
```cmd
gradlew :coupon-service:bootRun
```

### 방법 2: WAR 빌드 후 실행

```cmd
REM 빌드
build-general.bat
build-coupon.bat

REM 실행
java -jar general-service/build/libs/general-service-1.0.0.war
java -jar coupon-service/build/libs/coupon-service-1.0.0.war
```

### 방법 3: IDE에서 실행

**IntelliJ IDEA / Eclipse:**

1. **General Service 실행:**
   - Main Class: `com.ecommerce.general.GeneralServiceApplication`
   - VM Options: `-Dserver.port=8080`

2. **Coupon Service 실행:**
   - Main Class: `com.ecommerce.coupon.CouponServiceApplication`
   - VM Options: `-Dserver.port=8081`

---

## 서비스 접근 URL

### 개발 환경

**General Service (8080):**
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- User API: http://localhost:8080/api/v1/users/*
- Product API: http://localhost:8080/api/v1/products/*
- Order API: http://localhost:8080/api/v1/orders/*
- Images: http://localhost:8080/images/*

**Coupon Service (8081):**
- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health
- Coupon API: http://localhost:8081/api/v1/coupons/*

### 운영 환경 (ALB 통해 접근)

```
https://your-domain.com/api/v1/users/*     → General Service (EC2-2:8080)
https://your-domain.com/api/v1/products/*  → General Service (EC2-2:8080)
https://your-domain.com/api/v1/orders/*    → General Service (EC2-2:8080)
https://your-domain.com/api/v1/coupons/*   → Coupon Service (EC2-1:8081)
```

---

## 환경 변수 설정

### 개발 환경 (.env.dev)

```bash
# General Service
SERVER_PORT=8080
COUPON_SERVICE_URL=http://localhost:8081
PRODUCT_SERVICE_URL=http://localhost:8080
USER_SERVICE_URL=http://localhost:8080

# Coupon Service
SERVER_PORT=8081
```

### 운영 환경 (EC2 환경 변수)

**EC2-1 (Coupon Service):**
```bash
export SERVER_PORT=8081
export SPRING_PROFILES_ACTIVE=prd
export DB_HOST=your-rds-endpoint
export DB_NAME=coupon_db
export DB_USERNAME=admin
export DB_PASSWORD=your-password
export JWT_SECRET=your-jwt-secret
```

**EC2-2 (General Service):**
```bash
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prd
export DB_HOST=your-rds-endpoint
export DB_NAME=common_db
export DB_USERNAME=admin
export DB_PASSWORD=your-password
export JWT_SECRET=your-jwt-secret
export COUPON_SERVICE_URL=http://10.100.2.100:8081
export PRODUCT_SERVICE_URL=http://localhost:8080
export USER_SERVICE_URL=http://localhost:8080
```

---

## 포트 충돌 해결

### 포트가 이미 사용 중일 때

**Windows:**
```cmd
REM 포트 사용 프로세스 확인
netstat -ano | findstr :8080
netstat -ano | findstr :8081

REM 프로세스 종료
taskkill /PID <PID> /F
```

**Linux/Mac:**
```bash
# 포트 사용 프로세스 확인
lsof -i :8080
lsof -i :8081

# 프로세스 종료
kill -9 <PID>
```

---

## 서비스 간 통신

### General Service → Coupon Service

**개발 환경:**
```yaml
# application.yml
external:
  services:
    coupon-service:
      url: ${COUPON_SERVICE_URL:http://localhost:8081}
```

**운영 환경:**
```yaml
# application.yml (환경 변수로 주입)
external:
  services:
    coupon-service:
      url: http://10.100.2.100:8081  # EC2-1 Private IP
```

---

## 트러블슈팅

### 1. Coupon Service 연결 실패

**증상:** Order 생성 시 쿠폰 검증 실패

**해결:**
```cmd
REM Coupon Service가 실행 중인지 확인
curl http://localhost:8081/actuator/health

REM 환경 변수 확인
echo %COUPON_SERVICE_URL%
```

### 2. 포트 변경이 적용 안 됨

**해결:**
```cmd
REM 빌드 캐시 삭제
gradlew clean

REM 재빌드
gradlew :general-service:bootWar
gradlew :coupon-service:bootWar
```

### 3. 이미지 접근 403 에러

**증상:** `/images/**` 경로 접근 불가

**해결:** UserSecurityConfig에서 `/images/**` permitAll 설정 확인

---

## 요약

| 환경 | General Service | Coupon Service |
|------|----------------|----------------|
| 개발 | localhost:8080 | localhost:8081 |
| 운영 | EC2-2:8080 | EC2-1:8081 |
| ALB | 대상그룹 2 | 대상그룹 1 |

**중요:** 
- General Service는 User + Product + Order를 통합한 단일 서비스입니다
- Gradle 의존성으로 자동 통합되므로 소스 복사가 필요 없습니다
- 모든 서비스는 동일한 JWT_SECRET을 사용해야 합니다
