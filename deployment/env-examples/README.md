# 환경변수 설정 가이드

## 📋 환경별 파일 구성

```
deployment/env-examples/
├── .env.local    # 로컬 개발 환경 (IDE 직접 실행)
├── .env.dev      # AWS 개발 VPC (EC2 배포)
├── .env.prd      # AWS 운영 VPC (EC2 배포, Multi-AZ)
└── README.md     # 이 파일
```

---

## 🎯 환경별 특징

### .env.local (로컬 개발)
- **용도**: 개발자 로컬 PC에서 IDE로 직접 실행
- **DB**: localhost MySQL
- **서비스 통신**: localhost:8080, localhost:8081
- **특징**: 
  - 빠른 개발/테스트
  - 디버깅 용이
  - 외부 의존성 최소화

### .env.dev (AWS 개발 환경)
- **용도**: AWS Dev VPC의 EC2에 배포
- **DB**: RDS 개발 인스턴스
- **서비스 통신**: Private IP 또는 Internal NLB
- **특징**:
  - 실제 AWS 환경 테스트
  - 통합 테스트
  - 운영 환경 시뮬레이션

### .env.prd (AWS 운영 환경)
- **용도**: AWS Prd VPC의 EC2에 배포 (Multi-AZ)
- **DB**: RDS Multi-AZ
- **서비스 통신**: Internal NLB (권장) 또는 Route 53
- **특징**:
  - 고가용성 구성
  - 보안 강화
  - 모니터링 및 로깅

---

## 🔧 사용 방법

### 1. 로컬 개발 환경

**IDE 환경변수 설정 (IntelliJ IDEA):**

```
Run → Edit Configurations → Environment Variables
```

`.env.local` 파일 내용을 복사하여 설정

**또는 application-local.yml 사용:**

```yaml
# general-service/src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: root
    password: root
```

### 2. AWS 개발 환경 (EC2)

**EC2-1 (General Service):**

```bash
# setenv.sh 생성
sudo nano /opt/tomcat/bin/setenv.sh
```

`.env.dev` 파일 내용을 참고하여 작성:

```bash
#!/bin/bash
export SERVER_PORT=8080
export DB_URL="jdbc:mysql://dev-rds.xxxxx.rds.amazonaws.com:3306/ecommerce_dev_db"
export DB_USERNAME="dev_admin"
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret"
export COUPON_SERVICE_URL="http://172.31.10.100:8081"
# ... 나머지 환경변수
```

**EC2-2 (Coupon Service):**

```bash
sudo nano /opt/tomcat/bin/setenv.sh
```

```bash
#!/bin/bash
export SERVER_PORT=8081
export DB_URL="jdbc:mysql://dev-rds.xxxxx.rds.amazonaws.com:3306/coupon_dev_db"
export DB_USERNAME="dev_admin"
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret"
# ... 나머지 환경변수
```

### 3. AWS 운영 환경 (EC2 Multi-AZ)

**모든 General Service EC2 (AZ-A, AZ-C):**

```bash
sudo nano /opt/tomcat/bin/setenv.sh
```

```bash
#!/bin/bash
export SERVER_PORT=8080
export DB_URL="jdbc:mysql://prd-rds.xxxxx.rds.amazonaws.com:3306/ecommerce_prd_db"
export DB_USERNAME="prd_admin"
export DB_PASSWORD="your_strong_password"
export JWT_SECRET="your_strong_jwt_secret"

# Internal NLB 사용 (권장)
export COUPON_SERVICE_URL="http://coupon-prd-internal-lb.ap-northeast-2.elb.amazonaws.com:8081"

# 또는 Route 53 사용
# export COUPON_SERVICE_URL="http://coupon.prd.internal.ecommerce.local:8081"
```

**모든 Coupon Service EC2 (AZ-A, AZ-C):**

```bash
sudo nano /opt/tomcat/bin/setenv.sh
```

```bash
#!/bin/bash
export SERVER_PORT=8081
export DB_URL="jdbc:mysql://prd-rds.xxxxx.rds.amazonaws.com:3306/coupon_prd_db"
export DB_USERNAME="prd_admin"
export DB_PASSWORD="your_strong_password"
export JWT_SECRET="your_strong_jwt_secret"
```

---

## 🌐 서비스 간 통신 설정

### 단일 인스턴스 (개발)

```bash
# Private IP 직접 사용
COUPON_SERVICE_URL=http://172.31.10.100:8081
```

### Multi-AZ 이중화 (운영)

**방법 1: Internal NLB (권장)**

```bash
# NLB DNS 엔드포인트 사용
COUPON_SERVICE_URL=http://coupon-prd-internal-lb.ap-northeast-2.elb.amazonaws.com:8081
```

**장점:**
- AZ별 IP 신경 안 써도 됨
- 자동 헬스체크 및 장애 조치
- 로드밸런싱 자동 처리

**방법 2: Route 53 Private Hosted Zone**

```bash
# 커스텀 도메인 사용
COUPON_SERVICE_URL=http://coupon.prd.internal.ecommerce.local:8081
```

**장점:**
- 읽기 쉬운 도메인
- Weighted/Failover 라우팅 가능
- 헬스체크 기반 라우팅

자세한 내용은 `HA_DEPLOYMENT_GUIDE.md` 참고

---

## 🔐 보안 권장사항

### 1. 민감 정보 관리

**AWS Secrets Manager 사용 (권장):**

```bash
# Secret 생성
aws secretsmanager create-secret \
  --name prd/ecommerce/db \
  --secret-string '{"username":"prd_admin","password":"strong_password"}'

# Secret 조회
aws secretsmanager get-secret-value \
  --secret-id prd/ecommerce/db \
  --query SecretString \
  --output text
```

**setenv.sh에서 사용:**

```bash
#!/bin/bash

# AWS Secrets Manager에서 DB 정보 가져오기
DB_SECRET=$(aws secretsmanager get-secret-value \
  --secret-id prd/ecommerce/db \
  --query SecretString \
  --output text)

export DB_USERNAME=$(echo $DB_SECRET | jq -r .username)
export DB_PASSWORD=$(echo $DB_SECRET | jq -r .password)
```

### 2. 파일 권한 설정

```bash
# setenv.sh 권한 제한
sudo chmod 600 /opt/tomcat/bin/setenv.sh
sudo chown tomcat:tomcat /opt/tomcat/bin/setenv.sh
```

### 3. JWT Secret 관리

```bash
# 강력한 랜덤 키 생성
openssl rand -base64 64

# 환경별로 다른 키 사용
# 로컬: 개발용 키
# 개발: 개발 환경 전용 키
# 운영: 강력한 운영 키 (정기적 로테이션)
```

---

## 📋 환경변수 체크리스트

### 공통 (모든 환경)
- [ ] SERVER_PORT
- [ ] DB_URL
- [ ] DB_USERNAME
- [ ] DB_PASSWORD
- [ ] JWT_SECRET
- [ ] JWT_VALIDITY
- [ ] CORS_ALLOWED_ORIGINS

### General Service 추가
- [ ] COUPON_SERVICE_URL
- [ ] FILE_UPLOAD_DIR
- [ ] FILE_MAX_SIZE

### 운영 환경 추가
- [ ] DB_POOL_SIZE
- [ ] DB_MAX_LIFETIME
- [ ] DB_CONNECTION_TIMEOUT
- [ ] LOGGING_LEVEL_ROOT
- [ ] MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE

---

## 🔍 환경변수 확인 방법

### Tomcat 프로세스에서 확인

```bash
# Tomcat PID 확인
ps aux | grep tomcat

# 환경변수 확인
sudo cat /proc/{PID}/environ | tr '\0' '\n' | grep -E 'DB_|JWT_|COUPON_'
```

### 애플리케이션 로그에서 확인

```bash
# 시작 로그 확인
sudo tail -f /opt/tomcat/logs/catalina.out | grep -E 'Started|HikariPool'

# DB 연결 확인
sudo grep "HikariPool" /opt/tomcat/logs/catalina.out
```

### 헬스체크로 확인

```bash
# 서비스 정상 동작 확인
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

---

## 🛠️ 트러블슈팅

### 환경변수가 적용되지 않는 경우

```bash
# setenv.sh 실행 권한 확인
ls -l /opt/tomcat/bin/setenv.sh

# 권한 부여
sudo chmod +x /opt/tomcat/bin/setenv.sh

# Tomcat 완전 재시작
sudo systemctl stop tomcat
sleep 5
sudo systemctl start tomcat
```

### DB 연결 실패

```bash
# DB 접근 테스트
mysql -h your-rds-endpoint -u username -p

# 환경변수 확인
echo $DB_URL
echo $DB_USERNAME
```

### 서비스 간 통신 실패

```bash
# NLB 엔드포인트 테스트
curl http://coupon-internal-lb.ap-northeast-2.elb.amazonaws.com:8081/actuator/health

# DNS 조회
nslookup coupon.prd.internal.ecommerce.local
```

---

## 📚 참고 문서

- [EC2_ENV_SETUP_GUIDE.md](./EC2_ENV_SETUP_GUIDE.md) - EC2 환경변수 상세 설정
- [HA_DEPLOYMENT_GUIDE.md](./HA_DEPLOYMENT_GUIDE.md) - Multi-AZ 이중화 구성
- [SIMPLE_DEPLOY_GUIDE.md](./SIMPLE_DEPLOY_GUIDE.md) - 간단한 배포 가이드
