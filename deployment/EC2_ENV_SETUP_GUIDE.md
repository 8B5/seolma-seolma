# EC2 Tomcat WAR 배포 환경변수 설정 가이드

## 📋 개요

EC2에서 Tomcat으로 WAR 파일을 배포할 때 DB 연결, 서비스 간 통신을 위한 환경변수 설정 방법입니다.

---

## 🎯 배포 구조

```
EC2-1 (Coupon Service)
- Tomcat 10.1.x (Jakarta EE 9+ 지원)
- Port: 8081
- WAR: coupon-service.war
- DB: MySQL (RDS 또는 별도 서버)

EC2-2 (General Service)
- Tomcat 10.1.x (Jakarta EE 9+ 지원)
- Port: 8080
- WAR: general-service.war
- DB: MySQL (RDS 또는 별도 서버)
- 의존성: Coupon Service (EC2-1)
```

> **⚠️ 중요**: 이 프로젝트는 `jakarta.*` 패키지를 사용하므로 **Tomcat 10.1.x 이상** 필요합니다.
> Tomcat 9.x는 `javax.*` 패키지만 지원하므로 호환되지 않습니다.

---

## 🔧 방법 1: Tomcat setenv.sh 사용 (권장)

Tomcat의 `setenv.sh` 파일에 환경변수를 설정하는 방법입니다.

### EC2-1 (Coupon Service) 설정

```bash
# Tomcat bin 디렉토리로 이동
cd /opt/tomcat/bin

# setenv.sh 파일 생성
sudo nano setenv.sh
```

**setenv.sh 내용:**

```bash
#!/bin/bash

# JVM 옵션
export CATALINA_OPTS="$CATALINA_OPTS -Xms512m -Xmx1024m"

# 서버 포트
export SERVER_PORT=8081

# 데이터베이스 설정
export DB_HOST="your-rds-endpoint.xxxxx.ap-northeast-2.rds.amazonaws.com"
export DB_PORT=3306
export DB_NAME="coupon_db"
export DB_USERNAME="coupon_user"
export DB_PASSWORD="your_secure_password"

# JWT 설정
export JWT_SECRET="your-jwt-secret-key-min-256-bits-long"
export JWT_VALIDITY=3600

# CORS 설정
export CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com,http://localhost:3000"

# 로그 레벨
export LOGGING_LEVEL_ROOT=INFO
export LOGGING_LEVEL_APP=DEBUG
```

```bash
# 실행 권한 부여
sudo chmod +x setenv.sh
```

### EC2-2 (General Service) 설정

```bash
cd /opt/tomcat/bin
sudo nano setenv.sh
```

**setenv.sh 내용:**

```bash
#!/bin/bash

# JVM 옵션 (t3.nano용 - 권장하지 않음)
# export CATALINA_OPTS="$CATALINA_OPTS -Xms1024m -Xmx2048m"  # 원래 권장 설정
export CATALINA_OPTS="$CATALINA_OPTS -Xms256m -Xmx400m"      # t3.nano용 (위험)

# 서버 포트
export SERVER_PORT=8080

# 데이터베이스 설정 (User/Product/Order 통합 DB)
export DB_HOST="your-rds-endpoint.xxxxx.ap-northeast-2.rds.amazonaws.com"
export DB_PORT=3306
export DB_NAME="common_db"
export DB_USERNAME="general_user"
export DB_PASSWORD="your_secure_password"

# JWT 설정 (Coupon Service와 동일한 값 사용)
export JWT_SECRET="your-jwt-secret-key-min-256-bits-long"
export JWT_VALIDITY=3600

# 외부 서비스 URL (EC2-1의 Private IP 사용)
export COUPON_SERVICE_URL="http://172.31.x.x:8081"

# CORS 설정
export CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com,http://localhost:3000"

# 파일 저장 경로
export FILE_UPLOAD_DIR="/opt/tomcat/uploads"
export FILE_STORAGE_TYPE="local"

# 로그 레벨
export LOGGING_LEVEL_ROOT=INFO
export LOGGING_LEVEL_APP=DEBUG
```

```bash
sudo chmod +x setenv.sh
```

---

## 🔧 방법 2: 시스템 환경변수 사용

`/etc/environment` 또는 `.bashrc`에 설정하는 방법입니다.

### EC2-1 설정

```bash
sudo nano /etc/environment
```

```bash
# Coupon Service 환경변수
SERVER_PORT=8081
DB_HOST=${PRD_DB_HOST}
DB_PORT=3306
DB_NAME=${PRD_COUPON_DB_NAME}
DB_USERNAME=${PRD_COUPON_DB_USERNAME}
DB_PASSWORD=${PRD_COUPON_DB_PASSWORD}
JWT_SECRET="your-jwt-secret-key"
JWT_VALIDITY=3600
CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com"
```

### EC2-2 설정

```bash
sudo nano /etc/environment
```

```bash
# General Service 환경변수
SERVER_PORT=8080
DB_HOST=${PRD_DB_HOST}
DB_PORT=3306
DB_NAME=${PRD_GENERAL_DB_NAME}
DB_USERNAME=${PRD_GENERAL_DB_USERNAME}
DB_PASSWORD=${PRD_GENERAL_DB_PASSWORD}
JWT_SECRET="your-jwt-secret-key"
JWT_VALIDITY=3600
COUPON_SERVICE_URL="http://172.31.x.x:8081"
CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com"
FILE_UPLOAD_DIR="/opt/tomcat/uploads"
FILE_STORAGE_TYPE="local"
```

**적용:**

```bash
source /etc/environment
sudo systemctl restart tomcat
```

---

## 🔧 방법 3: Tomcat context.xml 사용

WAR별로 독립적인 설정이 필요한 경우 사용합니다.

```bash
sudo nano /opt/tomcat/conf/Catalina/localhost/general-service.xml
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="SERVER_PORT" value="8080" type="java.lang.String"/>
    <Environment name="DB_HOST" value="your-rds-endpoint.xxxxx.ap-northeast-2.rds.amazonaws.com" type="java.lang.String"/>
    <Environment name="DB_PORT" value="3306" type="java.lang.String"/>
    <Environment name="DB_NAME" value="common_db" type="java.lang.String"/>
    <Environment name="DB_USERNAME" value="general_user" type="java.lang.String"/>
    <Environment name="DB_PASSWORD" value="your_password" type="java.lang.String"/>
    <Environment name="JWT_SECRET" value="your-jwt-secret" type="java.lang.String"/>
    <Environment name="COUPON_SERVICE_URL" value="http://172.31.x.x:8081" type="java.lang.String"/>
</Context>
```

---

## 🌐 EC2 간 통신 설정

### 1. Private IP 확인

```bash
# EC2-1에서 실행
curl http://169.254.169.254/latest/meta-data/local-ipv4
# 예: 172.31.10.100

# EC2-2에서 실행
curl http://169.254.169.254/latest/meta-data/local-ipv4
# 예: 172.31.10.101
```

### 2. Security Group 설정

**EC2-1 (Coupon Service) Inbound Rules:**
```
Type: Custom TCP
Port: 8081
Source: EC2-2의 Security Group ID (sg-xxxxx)
Description: Allow from General Service
```

**EC2-2 (General Service) Inbound Rules:**
```
Type: Custom TCP
Port: 8080
Source: 0.0.0.0/0 (외부 접근 허용)
Description: Allow public access
```

### 3. 통신 테스트

```bash
# EC2-2에서 EC2-1로 통신 테스트
curl http://172.31.10.100:8081/internal/v1/coupons/health

# 응답 확인
{"status":"UP"}
```

---

## 📦 WAR 빌드 및 배포

### 1. WAR 파일 빌드

**로컬에서 실행:**

```bash
# Coupon Service 빌드
gradlew :coupon-service:clean :coupon-service:bootWar

# General Service 빌드
gradlew :general-service:clean :general-service:bootWar
```

**빌드 결과:**
- `coupon-service/build/libs/coupon-service.war`
- `general-service/build/libs/general-service.war`

### 2. EC2로 파일 전송

```bash
# EC2-1로 Coupon Service 전송
scp -i your-key.pem coupon-service/build/libs/coupon-service.war ec2-user@ec2-1-ip:/tmp/

# EC2-2로 General Service 전송
scp -i your-key.pem general-service/build/libs/general-service.war ec2-user@ec2-2-ip:/tmp/
```

### 3. Tomcat에 배포

**EC2-1에서:**

```bash
# 기존 배포 삭제
sudo rm -rf /opt/tomcat/webapps/coupon-service*

# 새 WAR 배포
sudo cp /tmp/coupon-service.war /opt/tomcat/webapps/

# Tomcat 재시작
sudo systemctl restart tomcat

# 로그 확인
sudo tail -f /opt/tomcat/logs/catalina.out
```

**EC2-2에서:**

```bash
sudo rm -rf /opt/tomcat/webapps/general-service*
sudo cp /tmp/general-service.war /opt/tomcat/webapps/
sudo systemctl restart tomcat
sudo tail -f /opt/tomcat/logs/catalina.out
```

---

## 🔍 환경변수 확인

### Tomcat 프로세스에서 확인

```bash
# Tomcat PID 확인
ps aux | grep tomcat

# 환경변수 확인
sudo cat /proc/{PID}/environ | tr '\0' '\n' | grep -E 'DB_|JWT_|COUPON_|SERVER_PORT'
```

### 애플리케이션 로그에서 확인

```bash
# 시작 로그 확인
sudo grep -A 20 "Started.*Application" /opt/tomcat/logs/catalina.out

# DB 연결 확인
sudo grep "HikariPool" /opt/tomcat/logs/catalina.out
```

---

## 🛠️ 트러블슈팅

### 1. 환경변수가 적용되지 않는 경우

```bash
# setenv.sh 권한 확인
ls -l /opt/tomcat/bin/setenv.sh

# 실행 권한이 없으면
sudo chmod +x /opt/tomcat/bin/setenv.sh

# Tomcat 완전 재시작
sudo systemctl stop tomcat
sleep 5
sudo systemctl start tomcat
```

### 2. DB 연결 실패

```bash
# DB 접근 테스트
mysql -h your-rds-endpoint -u general_user -p

# 방화벽 확인
telnet your-rds-endpoint 3306
```

### 3. EC2 간 통신 실패

```bash
# EC2-2에서 EC2-1 ping 테스트
ping 172.31.10.100

# 포트 확인
telnet 172.31.10.100 8081

# Security Group 확인
aws ec2 describe-security-groups --group-ids sg-xxxxx
```

### 4. 포트 충돌

```bash
# 포트 사용 확인
sudo netstat -tlnp | grep 8080

# 프로세스 종료
sudo kill -9 {PID}
```

### 5. WAR 컨텍스트 패스 문제

WAR 파일이 `general-service-1.0.0.war`로 배포되면 컨텍스트 패스가 `/general-service-1.0.0`이 됩니다.

**해결 방법 1: ROOT.war로 배포 (권장)**
```bash
# WAR 파일을 ROOT.war로 이름 변경하여 배포
sudo cp /tmp/general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo cp /tmp/coupon-service.war /opt/tomcat/webapps/ROOT.war
```

**해결 방법 2: 컨텍스트 패스 포함하여 접근**
```bash
# Health Check 경로
curl http://localhost:8080/general-service-1.0.0/actuator/health
curl http://localhost:8081/coupon/actuator/health

# API 경로
curl http://localhost:8080/general-service-1.0.0/api/v1/users/me
```

**ALB Health Check 설정:**
- General Service: `/general-service-1.0.0/actuator/health` 또는 ROOT.war 배포 시 `/actuator/health`
- Coupon Service: `/coupon/actuator/health` 또는 ROOT.war 배포 시 `/actuator/health`

---

## 📝 환경변수 체크리스트

### Coupon Service (EC2-1)
- [ ] SERVER_PORT=8081
- [ ] DB_HOST (Coupon DB 호스트)
- [ ] DB_PORT=3306
- [ ] DB_NAME (Coupon DB 이름)
- [ ] DB_USERNAME
- [ ] DB_PASSWORD
- [ ] JWT_SECRET
- [ ] JWT_VALIDITY
- [ ] CORS_ALLOWED_ORIGINS

### General Service (EC2-2)
- [ ] SERVER_PORT=8080
- [ ] DB_HOST (General DB 호스트)
- [ ] DB_PORT=3306
- [ ] DB_NAME (General DB 이름)
- [ ] DB_USERNAME
- [ ] DB_PASSWORD
- [ ] JWT_SECRET (Coupon과 동일)
- [ ] JWT_VALIDITY
- [ ] COUPON_SERVICE_URL (EC2-1 Private IP)
- [ ] CORS_ALLOWED_ORIGINS
- [ ] FILE_UPLOAD_DIR
- [ ] FILE_STORAGE_TYPE

### 네트워크
- [ ] EC2-1 Private IP 확인
- [ ] EC2-2 Private IP 확인
- [ ] Security Group 설정
- [ ] EC2 간 통신 테스트

---

## 🔐 보안 권장사항

1. **환경변수 파일 권한 설정**
```bash
sudo chmod 600 /opt/tomcat/bin/setenv.sh
sudo chown tomcat:tomcat /opt/tomcat/bin/setenv.sh
```

2. **DB 비밀번호는 AWS Secrets Manager 사용 권장**

3. **Private IP 사용**: EC2 간 통신은 반드시 Private IP 사용

4. **JWT Secret**: 최소 256비트 이상의 강력한 키 사용

5. **CORS**: 프로덕션에서는 정확한 도메인만 허용

---

## 💾 데이터베이스 정보

### MariaDB 버전
- **권장 버전**: MariaDB 10.6 이상
- **호환성**: MySQL 8.0과 호환
- **JDBC 드라이버**: `org.mariadb.jdbc.Driver`

### RDS 설정 예시
```bash
# RDS MariaDB 10.11.x 생성 시 설정
Engine: MariaDB
Version: 10.11.8
Instance Class: db.t3.micro (개발) / db.r5.large (운영)
Storage: 20GB (개발) / 100GB+ (운영)
Multi-AZ: No (개발) / Yes (운영)
```

---

## 📚 참고

- [Tomcat 10.1 Documentation](https://tomcat.apache.org/tomcat-10.1-doc/)
- [Jakarta EE 9 Migration Guide](https://jakarta.ee/specifications/platform/9/jakarta-platform-spec-9.html)
- [Spring Boot External Config](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [AWS EC2 Metadata](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html)

---

## 🔄 Tomcat 10.1 설치 (참고)

EC2에 Tomcat 10.1을 설치하는 방법:

```bash
# Java 17 설치 (필수)
sudo yum install java-17-amazon-corretto -y

# Tomcat 10.1 다운로드
cd /tmp
wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.33/bin/apache-tomcat-10.1.33.tar.gz

# 압축 해제 및 설치
sudo tar xzf apache-tomcat-10.1.33.tar.gz -C /opt
sudo mv /opt/apache-tomcat-10.1.33 /opt/tomcat

# 권한 설정
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat

# systemd 서비스 등록
sudo nano /etc/systemd/system/tomcat.service
```

**tomcat.service 내용:**

```ini
[Unit]
Description=Apache Tomcat 10.1
After=network.target

[Service]
Type=forking
User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
# 서비스 시작
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat
```

---

## ⚠️ t3.nano 배포 시 주의사항

### 메모리 부족 문제

**t3.nano 사양:**
- 메모리: 512MB
- General Service 권장 메모리: 2GB+

### t3.nano에서 강제 실행 시 설정

```bash
# 극도로 제한된 메모리 설정 (안정성 보장 불가)
export CATALINA_OPTS="$CATALINA_OPTS -Xms128m -Xmx350m -XX:MaxMetaspaceSize=128m"

# DB 커넥션 풀 최소화
export HIKARI_MAXIMUM_POOL_SIZE=3
export HIKARI_MINIMUM_IDLE=1

# 로그 레벨 최소화
export LOGGING_LEVEL_ROOT=WARN
export LOGGING_LEVEL_APP=ERROR
```

### 예상되는 문제점

1. **OutOfMemoryError 빈발**
2. **응답 속도 극도로 느림**
3. **동시 사용자 처리 불가 (1-2명 한계)**
4. **DB 커넥션 부족**
5. **JVM GC로 인한 잦은 멈춤**

### 권장 대안

```bash
# 개발환경 최소 사양
t3.small  (2GB RAM) - 약 $16/월
t3.medium (4GB RAM) - 약 $33/월

# 또는 Coupon Service만 t3.nano로 분리
EC2-1: t3.nano  (Coupon Service만)
EC2-2: t3.small (General Service)
```

### 모니터링 필수

t3.nano 사용 시 반드시 모니터링 설정:

```bash
# 메모리 사용량 실시간 모니터링
watch -n 1 'free -h && ps aux --sort=-%mem | head -10'

# JVM 메모리 모니터링
jstat -gc -t $(pgrep java) 5s
```

**결론: t3.nano는 General Service 운영에 부적합합니다. 최소 t3.small 권장합니다.**