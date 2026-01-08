#!/bin/bash

# WAR 파일 배포 스크립트
# 사용법: ./deploy.sh [service-name] [environment]
# 예시: ./deploy.sh user-service dev

SERVICE_NAME=$1
ENVIRONMENT=${2:-dev}
TOMCAT_HOME="/opt/tomcat"
BUILD_DIR="../${SERVICE_NAME}/build/libs"
WAR_FILE="${SERVICE_NAME}-1.0.0.war"

if [ -z "$SERVICE_NAME" ]; then
    echo "사용법: $0 [service-name] [environment]"
    echo "예시: $0 user-service dev"
    exit 1
fi

echo "=== ${SERVICE_NAME} 배포 시작 (환경: ${ENVIRONMENT}) ==="

# 1. WAR 파일 빌드
echo "1. WAR 파일 빌드 중..."
cd ../${SERVICE_NAME}
./gradlew clean bootWar -Pprofile=${ENVIRONMENT}

if [ ! -f "${BUILD_DIR}/${WAR_FILE}" ]; then
    echo "ERROR: WAR 파일을 찾을 수 없습니다: ${BUILD_DIR}/${WAR_FILE}"
    exit 1
fi

# 2. 서비스 중지
echo "2. Tomcat 서비스 중지 중..."
sudo systemctl stop tomcat-${SERVICE_NAME}

# 3. 기존 WAR 파일 백업
WEBAPPS_DIR="${TOMCAT_HOME}/instances/${SERVICE_NAME}/webapps"
if [ -f "${WEBAPPS_DIR}/${SERVICE_NAME}.war" ]; then
    echo "3. 기존 WAR 파일 백업 중..."
    sudo mv "${WEBAPPS_DIR}/${SERVICE_NAME}.war" "${WEBAPPS_DIR}/${SERVICE_NAME}.war.backup.$(date +%Y%m%d_%H%M%S)"
    sudo rm -rf "${WEBAPPS_DIR}/${SERVICE_NAME}"
fi

# 4. 새 WAR 파일 배포
echo "4. 새 WAR 파일 배포 중..."
sudo cp "${BUILD_DIR}/${WAR_FILE}" "${WEBAPPS_DIR}/${SERVICE_NAME}.war"
sudo chown tomcat:tomcat "${WEBAPPS_DIR}/${SERVICE_NAME}.war"

# 5. 환경 변수 설정
echo "5. 환경 변수 설정 중..."
case $ENVIRONMENT in
    "dev")
        export SPRING_PROFILES_ACTIVE=dev
        export DB_HOST=dev-db.internal
        export DB_NAME=ecommerce_dev
        export DB_USERNAME=dev_user
        export DB_PASSWORD=dev_password
        ;;
    "prd")
        export SPRING_PROFILES_ACTIVE=prd
        export DB_HOST=${PRD_DB_HOST}
        export DB_NAME=${PRD_DB_NAME}
        export DB_USERNAME=${PRD_DB_USERNAME}
        export DB_PASSWORD=${PRD_DB_PASSWORD}
        ;;
esac

# 6. 서비스 시작
echo "6. Tomcat 서비스 시작 중..."
sudo systemctl start tomcat-${SERVICE_NAME}

# 7. 배포 확인
echo "7. 배포 상태 확인 중..."
sleep 10

if sudo systemctl is-active --quiet tomcat-${SERVICE_NAME}; then
    echo "✅ ${SERVICE_NAME} 배포 성공!"
    echo "서비스 상태: $(sudo systemctl is-active tomcat-${SERVICE_NAME})"
    
    # Health Check
    case $SERVICE_NAME in
        "user-service")
            PORT=8080
            ;;
        "coupon-service")
            PORT=8081
            ;;
    esac
    
    echo "Health Check: curl http://localhost:${PORT}/actuator/health"
else
    echo "❌ ${SERVICE_NAME} 배포 실패!"
    echo "로그 확인: sudo journalctl -u tomcat-${SERVICE_NAME} -f"
    exit 1
fi

echo "=== 배포 완료 ==="