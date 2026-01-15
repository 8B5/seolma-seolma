# 고가용성(HA) 배포 가이드

## 📋 개요

Multi-AZ 이중화 구성에서 ALB/NLB를 사용한 서비스 간 통신 설정 방법입니다.

---

## 🏗️ 이중화 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                         VPC (10.0.0.0/16)                    │
│                                                               │
│  ┌──────────────────────┐      ┌──────────────────────┐    │
│  │   AZ-A (ap-ne-2a)    │      │   AZ-C (ap-ne-2c)    │    │
│  │                       │      │                       │    │
│  │  ┌────────────────┐  │      │  ┌────────────────┐  │    │
│  │  │ General-EC2-1A │  │      │  │ General-EC2-1C │  │    │
│  │  │ 10.0.1.10:8080 │  │      │  │ 10.0.3.10:8080 │  │    │
│  │  └────────────────┘  │      │  └────────────────┘  │    │
│  │          ↓            │      │          ↓            │    │
│  │  ┌────────────────┐  │      │  ┌────────────────┐  │    │
│  │  │ Coupon-EC2-2A  │  │      │  │ Coupon-EC2-2C  │  │    │
│  │  │ 10.0.1.20:8081 │  │      │  │ 10.0.3.20:8081 │  │    │
│  │  └────────────────┘  │      │  └────────────────┘  │    │
│  └──────────────────────┘      └──────────────────────┘    │
│           ↑                              ↑                   │
│           └──────────────┬───────────────┘                   │
│                          │                                   │
│              ┌───────────────────────┐                       │
│              │  Internal NLB/ALB     │                       │
│              │  coupon-internal.lb   │                       │
│              │  (DNS 엔드포인트)      │                       │
│              └───────────────────────┘                       │
│                                                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │              Public ALB (외부 접근)                 │     │
│  │         general-api.your-domain.com                │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 권장 구성

### 방법 1: Internal NLB 사용 (권장)

**장점:**
- DNS 기반 로드밸런싱으로 AZ별 IP 신경 안 써도 됨
- 자동 헬스체크 및 장애 조치
- 고정 엔드포인트로 설정 간편

**구성:**

```bash
# Coupon Service용 Internal NLB 생성
- Name: coupon-service-nlb
- Scheme: internal
- Type: Network Load Balancer
- Target Group: coupon-ec2-2a, coupon-ec2-2c (Port 8081)
- DNS: coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com
```

**환경변수 설정:**

```bash
# General Service의 setenv.sh
export COUPON_SERVICE_URL="http://coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com:8081"
```

### 방법 2: Route 53 Private Hosted Zone

**장점:**
- 커스텀 도메인 사용 가능
- 헬스체크 기반 라우팅
- Weighted/Failover 라우팅 정책 지원

**구성:**

```bash
# Route 53 Private Hosted Zone 생성
- Domain: internal.ecommerce.local
- VPC: 운영 VPC 연결

# A 레코드 생성
- Name: coupon.internal.ecommerce.local
- Type: A
- Routing: Weighted or Failover
- Value: 
  - 10.0.1.20 (AZ-A, Weight: 50)
  - 10.0.3.20 (AZ-C, Weight: 50)
```

**환경변수 설정:**

```bash
export COUPON_SERVICE_URL="http://coupon.internal.ecommerce.local:8081"
```

### 방법 3: Service Discovery (AWS Cloud Map)

**장점:**
- 자동 서비스 등록/해제
- 동적 서비스 디스커버리
- ECS/EKS와 통합 용이

---

## 📝 환경별 설정 예시

### 개발 환경 (Dev VPC)

**구성:**
- AZ-A: 1대, AZ-C: 1대 (총 2대)
- Internal NLB 사용

**.env.dev (General Service - EC2-1A, EC2-1C):**

```bash
# External Service URLs (Internal NLB 사용)
COUPON_SERVICE_URL=http://coupon-dev-internal-lb.ap-northeast-2.elb.amazonaws.com:8081

# 또는 Route 53 사용
COUPON_SERVICE_URL=http://coupon.dev.internal.ecommerce.local:8081
```

**.env.dev (Coupon Service - EC2-2A, EC2-2C):**

```bash
# Coupon Service는 외부 서비스 호출 없음
SERVER_PORT=8081
DB_URL=jdbc:mysql://dev-rds.xxxxx.rds.amazonaws.com:3306/coupon_dev_db
```

### 운영 환경 (Prd VPC)

**구성:**
- AZ-A: 2대, AZ-C: 2대 (총 4대)
- Internal NLB + Route 53 조합

**.env.prd (General Service - 모든 인스턴스 동일):**

```bash
# External Service URLs (Internal NLB 사용)
COUPON_SERVICE_URL=http://coupon-prd-internal-lb.ap-northeast-2.elb.amazonaws.com:8081

# 또는 Route 53 사용 (권장)
COUPON_SERVICE_URL=http://coupon.prd.internal.ecommerce.local:8081
```

**.env.prd (Coupon Service - 모든 인스턴스 동일):**

```bash
SERVER_PORT=8081
DB_URL=jdbc:mysql://prd-rds.xxxxx.rds.amazonaws.com:3306/coupon_prd_db
```

---

## 🔧 Internal NLB 생성 가이드

### 1. Target Group 생성

```bash
aws elbv2 create-target-group \
  --name coupon-service-tg \
  --protocol TCP \
  --port 8081 \
  --vpc-id vpc-xxxxx \
  --target-type instance \
  --health-check-enabled \
  --health-check-protocol HTTP \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 2
```

### 2. NLB 생성

```bash
aws elbv2 create-load-balancer \
  --name coupon-internal-nlb \
  --type network \
  --scheme internal \
  --subnets subnet-aaaa subnet-bbbb \
  --tags Key=Environment,Value=prd Key=Service,Value=coupon
```

### 3. Listener 생성

```bash
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:... \
  --protocol TCP \
  --port 8081 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:...
```

### 4. Target 등록

```bash
aws elbv2 register-targets \
  --target-group-arn arn:aws:elasticloadbalancing:... \
  --targets Id=i-ec2-2a Id=i-ec2-2c
```

### 5. DNS 확인

```bash
aws elbv2 describe-load-balancers \
  --names coupon-internal-nlb \
  --query 'LoadBalancers[0].DNSName' \
  --output text

# 출력: coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com
```

---

## 🔧 Route 53 Private Hosted Zone 설정

### 1. Hosted Zone 생성

```bash
aws route53 create-hosted-zone \
  --name internal.ecommerce.local \
  --vpc VPCRegion=ap-northeast-2,VPCId=vpc-xxxxx \
  --caller-reference $(date +%s) \
  --hosted-zone-config PrivateZone=true
```

### 2. A 레코드 생성 (Weighted Routing)

**AZ-A 레코드:**

```json
{
  "Changes": [{
    "Action": "CREATE",
    "ResourceRecordSet": {
      "Name": "coupon.internal.ecommerce.local",
      "Type": "A",
      "SetIdentifier": "AZ-A",
      "Weight": 50,
      "TTL": 60,
      "ResourceRecords": [{"Value": "10.0.1.20"}]
    }
  }]
}
```

**AZ-C 레코드:**

```json
{
  "Changes": [{
    "Action": "CREATE",
    "ResourceRecordSet": {
      "Name": "coupon.internal.ecommerce.local",
      "Type": "A",
      "SetIdentifier": "AZ-C",
      "Weight": 50,
      "TTL": 60,
      "ResourceRecords": [{"Value": "10.0.3.20"}]
    }
  }]
}
```

### 3. 헬스체크 추가 (선택)

```bash
aws route53 create-health-check \
  --health-check-config \
    IPAddress=10.0.1.20,Port=8081,Type=HTTP,ResourcePath=/actuator/health
```

---

## 🔍 통신 테스트

### NLB 엔드포인트 테스트

```bash
# General Service EC2에서 실행
curl http://coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com:8081/actuator/health

# 응답 확인
{"status":"UP"}

# 여러 번 호출하여 로드밸런싱 확인
for i in {1..10}; do
  curl -s http://coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com:8081/actuator/health | jq .
  sleep 1
done
```

### Route 53 DNS 테스트

```bash
# DNS 조회
nslookup coupon.internal.ecommerce.local

# 응답 확인 (Weighted 라우팅으로 IP가 번갈아 나옴)
Server:		10.0.0.2
Address:	10.0.0.2#53

Name:	coupon.internal.ecommerce.local
Address: 10.0.1.20

# 또는
Address: 10.0.3.20
```

### 실제 API 호출 테스트

```bash
# 쿠폰 조회 API 테스트
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com:8081/internal/v1/coupons/1
```

---

## 📋 배포 체크리스트

### 인프라 구성
- [ ] VPC 및 서브넷 생성 (Multi-AZ)
- [ ] Security Group 설정
  - [ ] General → Coupon 통신 허용
  - [ ] NLB → Coupon EC2 통신 허용
- [ ] Internal NLB 생성
- [ ] Target Group 생성 및 헬스체크 설정
- [ ] Route 53 Private Hosted Zone 생성 (선택)

### EC2 인스턴스
- [ ] AZ-A에 EC2 인스턴스 생성
- [ ] AZ-C에 EC2 인스턴스 생성
- [ ] Tomcat 10.1 설치
- [ ] 환경변수 설정 (NLB DNS 사용)
- [ ] WAR 파일 배포

### 환경변수
- [ ] COUPON_SERVICE_URL을 NLB DNS로 설정
- [ ] 모든 AZ의 인스턴스에 동일한 설정 적용
- [ ] DB 연결 정보 설정 (RDS Multi-AZ)
- [ ] JWT Secret 동기화

### 테스트
- [ ] 헬스체크 정상 동작 확인
- [ ] NLB 로드밸런싱 확인
- [ ] 서비스 간 통신 테스트
- [ ] 장애 조치 테스트 (한 AZ 다운)

---

## 🛠️ 트러블슈팅

### NLB 헬스체크 실패

```bash
# Target Group 상태 확인
aws elbv2 describe-target-health \
  --target-group-arn arn:aws:elasticloadbalancing:...

# Security Group 확인
# NLB → EC2 8081 포트 허용 확인

# EC2에서 헬스체크 엔드포인트 확인
curl http://localhost:8081/actuator/health
```

### DNS 조회 실패

```bash
# VPC DNS 설정 확인
aws ec2 describe-vpc-attribute \
  --vpc-id vpc-xxxxx \
  --attribute enableDnsHostnames

# enableDnsHostnames: true 확인

# Route 53 Hosted Zone VPC 연결 확인
aws route53 list-hosted-zones-by-vpc \
  --vpc-id vpc-xxxxx \
  --vpc-region ap-northeast-2
```

### 서비스 간 통신 실패

```bash
# General EC2에서 NLB 연결 테스트
telnet coupon-internal-xxxxx.ap-northeast-2.elb.amazonaws.com 8081

# Security Group 확인
# General EC2 SG → NLB SG 허용
# NLB SG → Coupon EC2 SG 허용
```

---

## 💡 베스트 프랙티스

1. **Internal NLB 사용**: 서비스 간 통신은 반드시 Internal LB 사용
2. **DNS 기반 통신**: IP 하드코딩 금지, DNS 엔드포인트 사용
3. **헬스체크 설정**: `/actuator/health` 엔드포인트 활용
4. **Connection Timeout**: RestTemplate/WebClient에 적절한 타임아웃 설정
5. **Circuit Breaker**: Resilience4j 등으로 장애 전파 방지
6. **로깅**: 서비스 간 호출 시 Trace ID 전파

---

## 🔐 보안 권장사항

1. **Private Subnet**: 모든 서비스는 Private Subnet에 배치
2. **Security Group**: 최소 권한 원칙 적용
3. **NLB Access Log**: S3에 로그 저장 활성화
4. **VPC Flow Log**: 네트워크 트래픽 모니터링
5. **IAM Role**: EC2에 최소 권한 IAM Role 부여

---

## 📚 참고

- [AWS NLB Documentation](https://docs.aws.amazon.com/elasticloadbalancing/latest/network/)
- [Route 53 Private Hosted Zones](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/hosted-zones-private.html)
- [AWS Cloud Map](https://docs.aws.amazon.com/cloud-map/)
