# 빠른 시작 가이드

## 🚀 로컬 개발 환경 실행

### General Service (User + Product + Order 통합)

**IntelliJ IDEA:**
- `GeneralServiceApplication` 우클릭 > Run

**VS Code:**
- F5 또는 Run > Start Debugging

**명령줄:**
```cmd
gradlew :general-service:bootRun
```

**확인:**
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

---

### Coupon Service

**IntelliJ IDEA:**
- `CouponServiceApplication` 우클릭 > Run

**명령줄:**
```cmd
gradlew :coupon-service:bootRun
```

**확인:**
- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health

---

## 📦 WAR 파일 빌드 (배포용)

### General Service 빌드
```cmd
build-general.bat
```

또는

```cmd
gradlew :general-service:clean :general-service:bootWar
```

**결과:** `general-service/build/libs/general-service-1.0.0.war`

### Coupon Service 빌드
```cmd
build-coupon.bat
```

또는

```cmd
gradlew :coupon-service:clean :coupon-service:bootWar
```

**결과:** `coupon-service/build/libs/coupon-service-1.0.0.war`

---

## 🏗️ 프로젝트 구조

**2개 서비스로 구성:**
- **General Service** (8080): User + Product + Order 통합
- **Coupon Service** (8081): 쿠폰 관리 전용

**장점:**
- ✅ Gradle 의존성으로 자동 통합 (소스 복사 불필요)
- ✅ Bean 충돌 없음
- ✅ EC2 2대 구조에 최적화
- ✅ 간단한 배포 구조

---

## 🔧 개발 워크플로우

### General Service 수정 시:
```
1. user-service, product-service, order-service 패키지에서 코드 수정
2. general-service는 Gradle 의존성으로 자동 포함
3. GeneralServiceApplication 재시작
```

### Coupon Service 수정 시:
```
1. coupon-service에서 코드 수정
2. CouponServiceApplication 재시작
```

---

## 🌐 포트 정보

| 서비스 | 개발 포트 | 운영 포트 |
|--------|----------|----------|
| General Service | 8080 | 8080 (EC2-2) |
| Coupon Service | 8081 | 8081 (EC2-1) |

---

## 📡 API 엔드포인트

### General Service (8080)
- User API: `/api/v1/users/**`
- Product API: `/api/v1/products/**`
- Order API: `/api/v1/orders/**`
- Admin API: `/api/v1/admin/**`
- Internal API: `/api/v1/internal/**`

### Coupon Service (8081)
- Coupon API: `/api/v1/coupons/**`
- Admin API: `/api/v1/admin/coupons/**`
- Internal API: `/api/v1/internal/coupons/**`

---

## 🔍 트러블슈팅

### 포트 충돌 (8080, 8081)
**증상:** Address already in use

**해결:**
```cmd
REM Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

REM Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Gradle 빌드 실패
**증상:** Gradle Wrapper 오류

**해결:** IDE에서 직접 빌드
- IntelliJ: Gradle 탭 > Tasks > build > bootWar
- 자세한 내용: [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)

### 서비스 간 통신 실패
**증상:** Order 생성 시 쿠폰 검증 실패

**해결:**
```cmd
REM Coupon Service 실행 확인
curl http://localhost:8081/actuator/health

REM 환경 변수 확인
echo %COUPON_SERVICE_URL%
```

### 이미지 403 에러
**증상:** `/images/**` 경로 접근 불가

**해결:** UserSecurityConfig에서 `/images/**` permitAll 설정 확인

---

## 📚 추가 문서

- [README.md](README.md) - 프로젝트 개요
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - API 명세서
- [deployment/SIMPLE_DEPLOY_GUIDE.md](deployment/SIMPLE_DEPLOY_GUIDE.md) - 배포 가이드
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - 빌드 가이드
- [PORT_GUIDE.md](PORT_GUIDE.md) - 포트 설정 가이드

---

## ✨ 요약

**로컬 실행:**
```cmd
gradlew :general-service:bootRun
gradlew :coupon-service:bootRun
```

**WAR 빌드:**
```cmd
build-general.bat
build-coupon.bat
```

**배포:**
- `general-service-1.0.0.war` → EC2-2 (8080)
- `coupon-service-1.0.0.war` → EC2-1 (8081)

끝!
