# 배포 가이드 - 대상그룹 2개 구조

## 아키텍처

```
ALB (Application Load Balancer)
 ├─ 대상그룹 1: EC2-1 (10.0.1.10:8081) - Coupon Service
 └─ 대상그룹 2: EC2-2 (10.0.1.20:8080) - General Service (User + Product + Order)
```

## 1. 사전 준비

### 1.1 프로젝트 빌드

```cmd
REM General Service 소스 복사 및 빌드
cd general-service
copy-sources.bat
cd ..
gradlew :general-service:clean :general-service:bootWar

REM Coupon Service 빌드
gradlew :coupon-service:clean :coupon-service:bootWar
```

빌드 결과:
- `general-service/build/libs/general-service-1.0.0.war`
- `coupon-service/build/libs/coupon-service-1.0.0.war`

## 2. EC2 인스턴스 설정

### 2.1 EC2-1 (Coupon Service)

```bash
# Tomcat 설치
sudo yum install -y java-21-amazon-corretto
wget https://downloads.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.tar.gz
sudo tar xzf apache-tomcat-10.1.17.tar.gz -C /opt
sudo mv /opt/apache-tomcat-10.1.17 /opt/tomcat
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat

# Systemd 서비스 생성
sudo tee /etc/systemd/system/tomcat.service > /dev/null <<EOF
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

# 환경 변수
Environment="SPRING_PROFILES_ACTIVE=prd"
Environment="SERVER_PORT=8081"
Environment="DB_HOST=your-db-host"
Environment="COUPON_DB_NAME=coupon_db"
Environment="COUPON_DB_USERNAME=your-username"
Environment="COUPON_DB_PASSWORD=your-password"
Environment="JWT_SECRET=your-jwt-secret"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
EOF

# WAR 파일 배포
sudo cp coupon-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

# 서비스 시작
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat

# 확인
curl http://localhost:8081/actuator/health
```

### 2.2 EC2-2 (General Service)

```bash
# Tomcat 설치 (EC2-1과 동일)
sudo yum install -y java-21-amazon-corretto
wget https://downloads.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.tar.gz
sudo tar xzf apache-tomcat-10.1.17.tar.gz -C /opt
sudo mv /opt/apache-tomcat-10.1.17 /opt/tomcat
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat

# Systemd 서비스 생성
sudo tee /etc/systemd/system/tomcat.service > /dev/null <<EOF
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

# 환경 변수
Environment="SPRING_PROFILES_ACTIVE=prd"
Environment="SERVER_PORT=8080"
Environment="DB_HOST=your-db-host"
Environment="COMMON_DB_NAME=common_db"
Environment="COMMON_DB_USERNAME=your-username"
Environment="COMMON_DB_PASSWORD=your-password"
Environment="JWT_SECRET=your-jwt-secret"
Environment="COUPON_SERVICE_URL=http://10.0.1.10:8081"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
EOF

# WAR 파일 배포
sudo cp general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

# 서비스 시작
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat

# 확인
curl http://localhost:8080/actuator/health
```

## 3. ALB 설정

### 3.1 대상 그룹 생성

**대상그룹 1: coupon-service-tg**
- 프로토콜: HTTP
- 포트: 8081
- 대상: EC2-1 (10.0.1.10:8081)
- Health Check: `/actuator/health`

**대상그룹 2: general-service-tg**
- 프로토콜: HTTP
- 포트: 8080
- 대상: EC2-2 (10.0.1.20:8080)
- Health Check: `/actuator/health`

### 3.2 ALB 리스너 규칙

**리스너: HTTP:80 (또는 HTTPS:443)**

규칙 우선순위:
1. 경로 `/api/v1/coupons/*` → coupon-service-tg
2. 경로 `/api/v1/admin/coupons/*` → coupon-service-tg
3. 기본 규칙 → general-service-tg

## 4. 보안 그룹 설정

### EC2-1 보안 그룹
- Inbound:
  - 8081 from ALB 보안 그룹
  - 8081 from EC2-2 보안 그룹 (Order Service가 Coupon Service 호출)

### EC2-2 보안 그룹
- Inbound:
  - 8080 from ALB 보안 그룹

### ALB 보안 그룹
- Inbound:
  - 80 from 0.0.0.0/0
  - 443 from 0.0.0.0/0 (HTTPS 사용 시)

## 5. 테스트

```bash
# ALB DNS 이름 확인
ALB_DNS="your-alb-dns-name.elb.amazonaws.com"

# General Service 테스트
curl http://$ALB_DNS/api/v1/products
curl http://$ALB_DNS/actuator/health

# Coupon Service 테스트
curl http://$ALB_DNS/api/v1/coupons
```

## 6. 모니터링

### CloudWatch 메트릭
- ALB: TargetResponseTime, HealthyHostCount, UnHealthyHostCount
- EC2: CPUUtilization, NetworkIn, NetworkOut

### 로그 확인
```bash
# EC2-1 (Coupon Service)
sudo tail -f /opt/tomcat/logs/catalina.out

# EC2-2 (General Service)
sudo tail -f /opt/tomcat/logs/catalina.out
```

## 7. 롤백 절차

```bash
# 이전 WAR 파일로 복구
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/ROOT*
sudo cp /backup/previous-version.war /opt/tomcat/webapps/ROOT.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
```

## 8. 주의사항

1. **포트 고정**: 
   - EC2-1: 반드시 8081 포트 사용
   - EC2-2: 반드시 8080 포트 사용

2. **서비스 간 통신**:
   - Order Service → Coupon Service 호출 시 Private IP 사용
   - COUPON_SERVICE_URL=http://10.0.1.10:8081

3. **데이터베이스**:
   - General Service: common_db 사용
   - Coupon Service: coupon_db 사용

4. **Health Check**:
   - 경로: `/actuator/health`
   - 정상 응답: HTTP 200

5. **배포 순서**:
   - Coupon Service 먼저 배포 (Order Service가 의존)
   - General Service 배포
