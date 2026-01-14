#!/bin/bash

# 쿠폰 서버 (EC2-2) 모니터링 스크립트
# 선착순 쿠폰 발급 시 리소스 사용량 모니터링

echo "=== 쿠폰 서버 모니터링 시작 ==="
echo "시간: $(date)"
echo

# CPU 사용률
echo "📊 CPU 사용률:"
top -bn1 | grep "Cpu(s)" | awk '{print $2 $3 $4 $5}'

# 메모리 사용률
echo
echo "💾 메모리 사용률:"
free -h | grep -E "Mem|Swap"

# 디스크 사용률
echo
echo "💿 디스크 사용률:"
df -h | grep -E "/$|/tmp"

# 네트워크 연결 수
echo
echo "🌐 네트워크 연결 수:"
ss -tuln | grep :8081 | wc -l

# Tomcat 프로세스 상태
echo
echo "☕ Tomcat 프로세스:"
ps aux | grep tomcat | grep -v grep | awk '{print "PID: " $2 ", CPU: " $3 "%, MEM: " $4 "%"}'

# 쿠폰 서비스 로그 (최근 10줄)
echo
echo "📝 쿠폰 서비스 로그 (최근 10줄):"
tail -n 10 /opt/tomcat/logs/catalina.out | grep -i coupon

echo
echo "=== 모니터링 완료 ==="