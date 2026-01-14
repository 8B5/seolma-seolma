@echo off
echo ========================================
echo General Service WAR 빌드
echo ========================================
echo.

REM General Service 빌드 (소스 복사 없이 Gradle 의존성 사용)
echo [1/2] General Service 빌드 중...
call gradlew :general-service:clean :general-service:bootWar

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
echo   general-service/build/libs/general-service-1.0.0.war
echo.
echo 다음 단계:
echo   1. EC2로 WAR 파일 전송
echo      scp -i your-key.pem general-service/build/libs/general-service-1.0.0.war ec2-user@ec2-ip:/tmp/
echo.
echo   2. EC2에서 배포
echo      sudo systemctl stop tomcat
echo      sudo rm -rf /opt/tomcat/webapps/ROOT*
echo      sudo cp /tmp/general-service-1.0.0.war /opt/tomcat/webapps/ROOT.war
echo      sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
echo      sudo systemctl start tomcat
echo.
pause
