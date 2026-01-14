# EC2 Tomcat 배포 가이드 (간단 버전)

## 🏗️ 아키텍처

```
EC2-1 (Coupon Service)    : 8081 포트
EC2-2 (General Service)   : 8080 포트 (User + Product + Order 통합)
```

---

## 📦 1. 로컬에서 WAR 파일 빌드

### Windows 환경

```cmd
REM 프로젝트 루트에서 실행

REM General Service 빌드
gradlew :general-service:clean :general-service:bootWar

REM Coupon Service 빌드
gradlew :coupon-service:clean :coupon-service:bootWar
```

### 빌드 결과 확인

```
general-service/build/libs/general-service-1.0.0.war
coupon-service/build/libs/coupon-service-1.0.0.war
```

---

## 🖥️ 2. EC2 초기 설정 (각 EC2에서 한 번만 실행)

### 2.1 Java 21 설치

```bash
# Amazon Linux 2023
sudo yum install -y java-21-amazon-corretto

# 설치 확인
java -version
```

### 2.2 Tomcat 10 설치

```bash
# Tomcat 다운로드
cd /tmp
wget https://downloads.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.tar.gz

# 압축 해제 및 설치
sudo tar xzf apache-tomcat-10.1.17.tar.gz -C /opt
sudo mv /opt/apache-tomcat-10.1.17 /opt/tomcat

# Tomcat 사용자 생성
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat
sudo chmod +x /opt/tomcat/bin/*.sh
```

### 2.3 Systemd 서비스 등록

#### EC2-1 (Coupon Service - 8081 포트)

```bash
sudo tee /etc/systemd/system/tomcat.service > /dev/null <<'EOF'
[Unit]
Description=Apache Tomcat - Coupon Service
After=network.target

[Service]
Type=forking
User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server"

# Spring Boot 환경 변수
Environment="SPRING_PROFILES_ACTIVE=prd"
Environment="SERVER_PORT=8081"

# Database 설정
Environment="DB_HOST=your-rds-endpoint.rds.amazonaws.com"
Environment="DB_PORT=3306"
Environment="DB_NAME=coupon_db"
Environment="DB_USERNAME=admin"
Environment="DB_PASSWORD=your-password"

# JWT 설정
Environment="JWT_SECRET=your-jwt-secret-key-at-least-256-bits-long"
Environment="JWT_VALIDITY=3600"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 서비스 등록
sudo systemctl daemon-reload
sudo systemctl enable tomcat
```

#### EC2-2 (General Service - 8080 포트)

```bash
sudo tee /etc/systemd/system/tomcat.service > /dev/null <<'EOF'
[Unit]
Description=Apache Tomcat - General Service
After=network.target

[Service]
Type=forking
User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_OPTS=-Xms1024M -Xmx2048M -server"

# Spring Boot 환경 변수
Environment="SPRING_PROFILES_ACTIVE=prd"
Environment="SERVER_PORT=8080"

# Database 설정
Environment="DB_HOST=your-rds-endpoint.rds.amazonaws.com"
Environment="DB_PORT=3306"
Environment="DB_NAME=common_db"
Environment="DB_USERNAME=admin"
Environment="DB_PASSWORD=your-password"

# JWT 설정
Environment="JWT_SECRET=your-jwt-secret-key-at-least-256-bits-long"
Environment="JWT_VALIDITY=3600"

# 외부 서비스 URL (EC2-1의 Private IP 사용)
Environment="COUPON_SERVICE_URL=http://10.0.1.10:8081"

# 파일 스토리지 설정
Environment="FILE_STORAGE_TYPE=s3"
Environment="S3_BUCKET_NAME=your-bucket-name"
Environment="AWS_REGION=ap-northeast-2"

# CORS 설정
Environment="CORS_ALLOWED_ORIGINS=https://your-domain.com"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 서비스 등록
sudo systemctl daemon-reload
sudo systemctl enable tomcat
```

---

## 🚀 3. WAR 파일 배포

### 3.1 로컬에서 EC2로 WAR 파일 전송

```bash
# EC2-1 (Coupon Service)
scp -i your-key.pem coupon-service/build/libs/coupon-service-1.0.0.war ec2-user@ec2-1-ip:/tmp/

# EC2-2 (General Service)
scp -i your-key.pem general-service/build/libs/general-service-1.0.0.war ec2-user@ec2-2-ip:/tmp/
```

### 3.2 EC2에서 배포 실행

#### EC2-1 (Coupon Service)

```bash
# EC2-1에 SSH 접속
ssh -i your-key.pem ec2-user@ec2-1-ip

# Tomcat 중지
sudo systemctl stop tomcat

# 기존 WAR 파일 제거
sudo rm -rf /opt/tomcat/webapps/ROOT*

# 새 WAR 파일 배포 (ROOT.war로 이름 변경 - 루트 경로로 배포)
sudo cp /tmp/coupon-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

# Tomcat 시작
sudo systemctl start tomcat

# 로그 확인
sudo tail -f /opt/tomcat/logs/catalina.out
```

#### EC2-2 (General Service)

```bash
# EC2-2에 SSH 접속
ssh -i your-key.pem ec2-user@ec2-2-ip

# Tomcat 중지
sudo systemctl stop tomcat

# 기존 WAR 파일 제거
sudo rm -rf /opt/tomcat/webapps/ROOT*

# 새 WAR 파일 배포 (ROOT.war로 이름 변경 - 루트 경로로 배포)
sudo cp /tmp/general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

# Tomcat 시작
sudo systemctl start tomcat

# 로그 확인
sudo tail -f /opt/tomcat/logs/catalina.out
```

---

## ✅ 4. 배포 확인

### 4.1 Health Check

```bash
# EC2-1 (Coupon Service)
curl http://localhost:8081/actuator/health

# EC2-2 (General Service)
curl http://localhost:8080/actuator/health
```

### 4.2 API 테스트

```bash
# EC2-2에서 EC2-1 호출 테스트 (서비스 간 통신)
curl http://10.0.1.10:8081/actuator/health

# 외부에서 ALB를 통한 테스트
curl http://your-alb-dns/api/v1/products
curl http://your-alb-dns/api/v1/coupons
```

---

## 🔧 5. 트러블슈팅

### 서비스 상태 확인

```bash
# 서비스 상태
sudo systemctl status tomcat

# 로그 실시간 확인
sudo tail -f /opt/tomcat/logs/catalina.out

# 최근 로그 확인
sudo journalctl -u tomcat -n 100 --no-pager
```

### 서비스 재시작

```bash
sudo systemctl restart tomcat
```

### 포트 확인

```bash
# 포트 리스닝 확인
sudo netstat -tlnp | grep java

# 또는
sudo ss -tlnp | grep java
```

### 환경 변수 확인

```bash
# Systemd 서비스의 환경 변수 확인
sudo systemctl show tomcat | grep Environment
```

---

## 📝 6. 주요 포인트

### WAR 파일 이름이 중요합니다!

- `ROOT.war` → 루트 경로로 배포 (`http://localhost:8080/`)
- `myapp.war` → `/myapp` 경로로 배포 (`http://localhost:8080/myapp/`)

**우리 프로젝트는 ROOT.war로 배포해야 합니다!**

### 환경 변수 수정 시

```bash
# /etc/systemd/system/tomcat.service 파일 수정 후
sudo systemctl daemon-reload
sudo systemctl restart tomcat
```

### 데이터베이스 연결

- RDS 보안 그룹에서 EC2 보안 그룹 허용 필요
- 포트: 3306 (MariaDB/MySQL)

### 서비스 간 통신

- EC2-2 (General Service)에서 EC2-1 (Coupon Service) 호출
- **Private IP 사용**: `http://10.0.1.10:8081`
- EC2-1 보안 그룹에서 EC2-2 보안 그룹의 8081 포트 허용 필요

---

## 🔄 7. 재배포 (업데이트) 절차

```bash
# 1. 로컬에서 빌드
gradlew :general-service:clean :general-service:bootWar

# 2. EC2로 전송
scp -i your-key.pem general-service/build/libs/general-service-1.0.0.war ec2-user@ec2-ip:/tmp/

# 3. EC2에서 배포
ssh -i your-key.pem ec2-user@ec2-ip
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/ROOT*
sudo cp /tmp/general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
sudo tail -f /opt/tomcat/logs/catalina.out
```

---

## 📊 8. 모니터링

### CloudWatch 설정 (선택사항)

```bash
# CloudWatch Agent 설치
sudo yum install -y amazon-cloudwatch-agent

# Tomcat 로그를 CloudWatch로 전송
sudo tee /opt/aws/amazon-cloudwatch-agent/etc/config.json > /dev/null <<'EOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/opt/tomcat/logs/catalina.out",
            "log_group_name": "/aws/ec2/tomcat",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
EOF

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json
```

---

## 🎯 요약

1. **로컬**: WAR 파일 빌드 (`gradlew bootWar`)
2. **전송**: SCP로 EC2에 업로드
3. **배포**: `/opt/tomcat/webapps/ROOT.war`로 복사
4. **시작**: `sudo systemctl start tomcat`
5. **확인**: `curl http://localhost:8080/actuator/health`

끝!
