# General Service (통합 서비스)

User, Product, Order 서비스를 하나의 WAR 파일로 통합한 서비스입니다.

## 구조

```
EC2-1: 8081 (Coupon Service)
EC2-2: 8080 (General Service - User + Product + Order)
```

## 빌드 방법

### 1. 소스 코드 복사

```cmd
cd general-service
copy-sources.bat
```

이 스크립트는 다음 작업을 수행합니다:
- user-service, product-service, order-service의 소스를 복사
- 중복되는 설정 파일 제거 (SecurityConfig, OpenApiConfig, Application 클래스)

### 2. WAR 파일 빌드

```cmd
cd ..
gradlew :general-service:clean :general-service:bootWar
```

빌드 결과: `general-service/build/libs/general-service-1.0.0.war`

## 배포 방법

### EC2-2에 배포

```bash
# WAR 파일 업로드
scp general-service/build/libs/general-service-1.0.0.war ec2-user@<EC2-2-IP>:/tmp/

# EC2-2에 접속
ssh ec2-user@<EC2-2-IP>

# Tomcat에 배포
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/ROOT*
sudo cp /tmp/general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
```

## 환경 변수

```bash
# Database
export DB_HOST=your-db-host
export COMMON_DB_NAME=common_db
export COMMON_DB_USERNAME=your-username
export COMMON_DB_PASSWORD=your-password

# JWT
export JWT_SECRET=your-jwt-secret
export JWT_VALIDITY=3600

# External Services
export COUPON_SERVICE_URL=http://<EC2-1-IP>:8081

# File Storage
export FILE_STORAGE_TYPE=s3
export S3_BUCKET_NAME=your-bucket
export AWS_REGION=ap-northeast-2

# Server
export SERVER_PORT=8080
```

## API 엔드포인트

### User Service
- POST `/api/v1/auth/signup` - 회원가입
- POST `/api/v1/auth/login` - 로그인
- GET `/api/v1/users/me` - 내 정보 조회

### Product Service
- GET `/api/v1/products` - 상품 목록 조회
- GET `/api/v1/products/{id}` - 상품 상세 조회
- POST `/api/v1/admin/products` - 상품 등록 (관리자)
- PUT `/api/v1/admin/products/{id}` - 상품 수정 (관리자)

### Order Service
- POST `/api/v1/orders` - 주문 생성
- GET `/api/v1/orders` - 내 주문 목록
- GET `/api/v1/orders/{id}` - 주문 상세 조회
- GET `/api/v1/admin/orders` - 전체 주문 조회 (관리자)

## Swagger UI

http://your-server:8080/swagger-ui.html

## Health Check

http://your-server:8080/actuator/health

## 주의사항

1. **데이터베이스**: User, Product, Order는 같은 DB(common_db)를 사용합니다
2. **Coupon Service**: 별도 EC2에서 실행되며, Order Service가 WebClient로 호출합니다
3. **포트**: 반드시 8080 포트로 실행해야 합니다 (ALB 대상그룹 설정)
4. **파일 업로드**: Product 이미지는 S3 또는 로컬 스토리지 사용

## 트러블슈팅

### 빌드 실패 시
```cmd
gradlew clean
del /s /q general-service\src\main\java\com\ecommerce\user
del /s /q general-service\src\main\java\com\ecommerce\product
del /s /q general-service\src\main\java\com\ecommerce\order
copy-sources.bat
gradlew :general-service:bootWar
```

### 서비스 간 통신 실패 시
- COUPON_SERVICE_URL 환경 변수 확인
- EC2-1과 EC2-2 간 보안 그룹 설정 확인
- Coupon Service가 정상 실행 중인지 확인
