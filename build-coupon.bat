@echo off
echo ========================================
echo Coupon Service WAR 빌드
echo ========================================
echo.

REM Coupon Service 빌드
echo [1/2] Coupon Service 빌드 중...
call gradlew :coupon-service:clean :coupon-service:bootWar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 빌드 실패!
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 빌드 완료!
echo ========================================
echo.
echo 빌드 결과:
echo   coupon-service/build/libs/coupon-service-1.0.0.war
echo.
echo 다음 단계:
echo   1. EC2로 WAR 파일 전송
echo      scp -i your-key.pem coupon-service/build/libs/coupon-service-1.0.0.war ec2-user@ec2-ip:/tmp/
echo.
echo   2. EC2에서 배포
echo      sudo systemctl stop tomcat
echo      sudo rm -rf /opt/tomcat/webapps/ROOT*
echo      sudo cp /tmp/coupon-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
echo      sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
echo      sudo systemctl start tomcat
echo.
pause
