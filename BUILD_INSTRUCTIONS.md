# General Service 빌드 가이드

Gradle Wrapper가 손상되어 있어서 IDE를 통해 빌드해야 합니다.

## 방법 1: IntelliJ IDEA에서 빌드 (권장)

1. **프로젝트 열기**
   - IntelliJ IDEA에서 프로젝트 루트 열기

2. **Gradle 새로고침**
   - 우측 Gradle 탭 클릭
   - 새로고침 버튼 클릭

3. **General Service 빌드**
   - Gradle 탭에서: `general-service` > `Tasks` > `build` > `bootWar` 더블클릭
   
   또는 터미널에서:
   ```
   ./gradlew :general-service:bootWar
   ```

4. **빌드 결과 확인**
   - `general-service/build/libs/general-service-1.0.0.war`

## 방법 2: Eclipse에서 빌드

1. **프로젝트 Import**
   - File > Import > Gradle > Existing Gradle Project

2. **Gradle Tasks 실행**
   - 프로젝트 우클릭 > Run As > Gradle Build
   - Tasks: `bootWar`
   - Working Directory: `general-service`

## 방법 3: VS Code에서 빌드

1. **Gradle Extension 설치**
   - Gradle for Java 확장 설치

2. **Gradle Tasks 실행**
   - Gradle 탭에서 `general-service` > `build` > `bootWar` 실행

## 방법 4: Gradle Wrapper 복구 후 빌드

Gradle이 시스템에 설치되어 있다면:

```cmd
REM Gradle Wrapper 재생성
gradle wrapper --gradle-version 8.5

REM 빌드
gradlew :general-service:bootWar
```

## 빌드 전 체크리스트

- [ ] `general-service/copy-sources.bat` 실행 완료
- [ ] User, Product, Order 소스가 `general-service/src/main/java`에 복사됨
- [ ] `general-service/src/main/resources/application.yml` 존재
- [ ] `general-service/src/main/java/com/ecommerce/general/GeneralServiceApplication.java` 존재

## 트러블슈팅

### 1. "Cannot find symbol" 에러

**원인:** 소스가 복사되지 않음

**해결:**
```cmd
cd general-service
copy-sources.bat
```

### 2. "Duplicate class" 에러

**원인:** SecurityConfig 등이 중복됨

**해결:** 다음 파일들을 삭제:
- `src/main/java/com/ecommerce/user/config/SecurityConfig.java`
- `src/main/java/com/ecommerce/product/config/SecurityConfig.java`
- `src/main/java/com/ecommerce/order/config/SecurityConfig.java`
- `src/main/java/com/ecommerce/*/config/OpenApiConfig.java`
- `src/main/java/com/ecommerce/*/*ServiceApplication.java`

### 3. Gradle Wrapper 에러

**원인:** `gradle/wrapper/gradle-wrapper.jar` 파일 손상

**해결:**
1. 다른 프로젝트에서 정상적인 gradle-wrapper.jar 복사
2. 또는 Gradle 재설치 후 `gradle wrapper` 실행
3. 또는 IDE에서 직접 빌드

## Coupon Service 빌드

Coupon Service는 정상적으로 빌드 가능합니다:

```cmd
gradlew :coupon-service:bootWar
```

또는 IDE에서:
- Gradle 탭 > `coupon-service` > `Tasks` > `build` > `bootWar`

## 최종 결과물

빌드 성공 시 다음 파일들이 생성됩니다:

```
general-service/build/libs/general-service-1.0.0.war  (약 50-60MB)
coupon-service/build/libs/coupon-service-1.0.0.war    (약 40-50MB)
```

## 배포

생성된 WAR 파일을:
- `general-service-1.0.0.war` → EC2-2 (포트 8080)
- `coupon-service-1.0.0.war` → EC2-1 (포트 8081)

로 배포하세요.
