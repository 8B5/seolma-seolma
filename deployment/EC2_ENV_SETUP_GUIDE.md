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
export DB_URL="jdbc:mysql://your-rds-endpoint:3306/coupon_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true"
export DB_USERNAME="coupon_user"
export DB_PASSWORD="your_secure_password"

# JWT 설정
export JWT_SECRET="your-jwt-secret-key-min-256-bits-long"
export JWT_EXPIRATION=3600000

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

# JVM 옵션
export CATALINA_OPTS="$CATALINA_OPTS -Xms1024m -Xmx2048m"

# 서버 포트
export SERVER_PORT=8080

# 데이터베이스 설정 (User/Product/Order 통합 DB)
export DB_URL="jdbc:mysql://your-rds-endpoint:3306/general_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true"
export DB_USERNAME="general_user"
export DB_PASSWORD="your_secure_password"

# JWT 설정 (Coupon Service와 동일한 값 사용)
export JWT_SECRET="your-jwt-secret-key-min-256-bits-long"
export JWT_EXPIRATION=3600000

# 외부 서비스 URL (EC2-1의 Private IP 사용)
export COUPON_SERVICE_URL="http://172.31.x.x:8081"

# CORS 설정
export CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com,http://localhost:3000"

# 파일 저장 경로
export FILE_UPLOAD_DIR="/opt/tomcat/uploads"
export FILE_MAX_SIZE=10485760

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
DB_URL="jdbc:mysql://your-rds-endpoint:3306/coupon_db?useSSL=false&serverTimezone=Asia/Seoul"
DB_USERNAME="coupon_user"
DB_PASSWORD="your_secure_password"
JWT_SECRET="your-jwt-secret-key"
JWT_EXPIRATION=3600000
CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com"
```

### EC2-2 설정

```bash
sudo nano /etc/environment
```

```bash
# General Service 환경변수
SERVER_PORT=8080
DB_URL="jdbc:mysql://your-rds-endpoint:3306/general_db?useSSL=false&serverTimezone=Asia/Seoul"
DB_USERNAME="general_user"
DB_PASSWORD="your_secure_password"
JWT_SECRET="your-jwt-secret-key"
JWT_EXPIRATION=3600000
COUPON_SERVICE_URL="http://172.31.x.x:8081"
CORS_ALLOWED_ORIGINS="http://your-frontend-domain.com"
FILE_UPLOAD_DIR="/opt/tomcat/uploads"
FILE_MAX_SIZE=10485760
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
    <Environment name="DB_URL" value="jdbc:mysql://your-rds:3306/general_db" type="java.lang.String"/>
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
sudo cat /proc/{PID}/environ | tr '\0' '\n' | grep -E 'DB_|JWT_|COUPON_'
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

---

## 📝 환경변수 체크리스트

### Coupon Service (EC2-1)
- [ ] SERVER_PORT=8081
- [ ] DB_URL (Coupon DB)
- [ ] DB_USERNAME
- [ ] DB_PASSWORD
- [ ] JWT_SECRET
- [ ] CORS_ALLOWED_ORIGINS

### General Service (EC2-2)
- [ ] SERVER_PORT=8080
- [ ] DB_URL (General DB)
- [ ] DB_USERNAME
- [ ] DB_PASSWORD
- [ ] JWT_SECRET (Coupon과 동일)
- [ ] COUPON_SERVICE_URL (EC2-1 Private IP)
- [ ] CORS_ALLOWED_ORIGINS
- [ ] FILE_UPLOAD_DIR

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