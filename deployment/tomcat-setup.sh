#!/bin/bash

# Tomcat 10 설치 및 설정 스크립트
# EC2에서 실행하여 여러 WAR 파일을 배포할 수 있도록 구성

# 변수 설정
TOMCAT_VERSION="10.1.17"
TOMCAT_HOME="/opt/tomcat"
JAVA_HOME="/usr/lib/jvm/java-21-openjdk"

# Tomcat 사용자 생성
sudo useradd -r -m -U -d $TOMCAT_HOME -s /bin/false tomcat

# Tomcat 다운로드 및 설치
cd /tmp
wget https://downloads.apache.org/tomcat/tomcat-10/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz
sudo tar xf apache-tomcat-${TOMCAT_VERSION}.tar.gz -C /opt/tomcat --strip-components=1

# 권한 설정
sudo chown -R tomcat: $TOMCAT_HOME
sudo sh -c 'chmod +x /opt/tomcat/bin/*.sh'

# 서비스별 포트 설정을 위한 server.xml 백업
sudo cp $TOMCAT_HOME/conf/server.xml $TOMCAT_HOME/conf/server.xml.backup

# User Service용 Tomcat 인스턴스 (포트 8080)
sudo mkdir -p $TOMCAT_HOME/instances/user-service/{conf,logs,temp,work,webapps}
sudo cp -r $TOMCAT_HOME/conf/* $TOMCAT_HOME/instances/user-service/conf/

# Product Service용 Tomcat 인스턴스 (포트 8081)
sudo mkdir -p $TOMCAT_HOME/instances/product-service/{conf,logs,temp,work,webapps}
sudo cp -r $TOMCAT_HOME/conf/* $TOMCAT_HOME/instances/product-service/conf/

# Coupon Service용 Tomcat 인스턴스 (포트 8081)
sudo mkdir -p $TOMCAT_HOME/instances/coupon-service/{conf,logs,temp,work,webapps}
sudo cp -r $TOMCAT_HOME/conf/* $TOMCAT_HOME/instances/coupon-service/conf/

# User Service server.xml 설정 (포트 8080)
sudo tee $TOMCAT_HOME/instances/user-service/conf/server.xml > /dev/null <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<Server port="8005" shutdown="SHUTDOWN">
  <Service name="Catalina">
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               maxThreads="200"
               minSpareThreads="10" />
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Context path="" docBase="user-service" />
      </Host>
    </Engine>
  </Service>
</Server>
EOF

# Coupon Service server.xml 설정 (포트 8081)
sudo tee $TOMCAT_HOME/instances/coupon-service/conf/server.xml > /dev/null <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<Server port="8006" shutdown="SHUTDOWN">
  <Service name="Catalina">
    <Connector port="8081" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               maxThreads="200"
               minSpareThreads="10" />
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Context path="" docBase="coupon-service" />
      </Host>
    </Engine>
  </Service>
</Server>
EOF

# 권한 설정
sudo chown -R tomcat: $TOMCAT_HOME/instances

# Systemd 서비스 파일 생성 - User Service
sudo tee /etc/systemd/system/tomcat-user.service > /dev/null <<EOF
[Unit]
Description=Apache Tomcat Web Application Container - User Service
After=network.target

[Service]
Type=forking
Environment="JAVA_HOME=$JAVA_HOME"
Environment="CATALINA_PID=$TOMCAT_HOME/instances/user-service/temp/tomcat.pid"
Environment="CATALINA_HOME=$TOMCAT_HOME"
Environment="CATALINA_BASE=$TOMCAT_HOME/instances/user-service"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"
Environment="JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"
Environment="SPRING_PROFILES_ACTIVE=\${ENVIRONMENT:dev}"

ExecStart=$TOMCAT_HOME/bin/startup.sh
ExecStop=$TOMCAT_HOME/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Systemd 서비스 파일 생성 - Coupon Service
sudo tee /etc/systemd/system/tomcat-coupon.service > /dev/null <<EOF
[Unit]
Description=Apache Tomcat Web Application Container - Coupon Service
After=network.target

[Service]
Type=forking
Environment="JAVA_HOME=$JAVA_HOME"
Environment="CATALINA_PID=$TOMCAT_HOME/instances/coupon-service/temp/tomcat.pid"
Environment="CATALINA_HOME=$TOMCAT_HOME"
Environment="CATALINA_BASE=$TOMCAT_HOME/instances/coupon-service"
Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"
Environment="JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"
Environment="SPRING_PROFILES_ACTIVE=\${ENVIRONMENT:dev}"

ExecStart=$TOMCAT_HOME/bin/startup.sh
ExecStop=$TOMCAT_HOME/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# 서비스 등록 및 시작
sudo systemctl daemon-reload
sudo systemctl enable tomcat-user
sudo systemctl enable tomcat-coupon

echo "Tomcat 설치 완료!"
echo "User Service: http://localhost:8080"
echo "Coupon Service: http://localhost:8081"
echo ""
echo "서비스 시작: sudo systemctl start tomcat-user tomcat-coupon"
echo "서비스 상태: sudo systemctl status tomcat-user tomcat-coupon"