# AWS S3 이미지 저장소 마이그레이션 가이드

## 📋 개요

로컬 파일 시스템에서 AWS S3로 이미지 저장소를 마이그레이션하는 방법을 설명합니다.

---

## 🎯 왜 S3를 사용해야 하나요?

### 로컬 파일 시스템의 문제점
- ❌ EC2 재시작 시 파일 손실 위험
- ❌ 여러 EC2 인스턴스 간 파일 공유 불가
- ❌ 백업 및 복구 어려움
- ❌ CDN 연동 불가

### S3의 장점
- ✅ 99.999999999% (11 9's) 내구성
- ✅ 무제한 저장 공간
- ✅ CloudFront CDN 연동 가능
- ✅ 자동 백업 및 버전 관리
- ✅ 여러 EC2에서 동일한 파일 접근 가능

---

## 🚀 1단계: AWS S3 버킷 생성

### 1.1 S3 버킷 생성

```bash
# AWS CLI로 버킷 생성
aws s3 mb s3://your-ecommerce-images --region ap-northeast-2

# 또는 AWS Console에서:
# 1. S3 콘솔 접속
# 2. "버킷 만들기" 클릭
# 3. 버킷 이름: your-ecommerce-images
# 4. 리전: 아시아 태평양(서울) ap-northeast-2
# 5. "버킷 만들기" 클릭
```

### 1.2 버킷 정책 설정 (Public Read)

이미지를 공개적으로 접근 가능하게 하려면:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-ecommerce-images/*"
    }
  ]
}
```

**적용 방법:**
1. S3 콘솔 > 버킷 선택
2. "권한" 탭
3. "버킷 정책" > "편집"
4. 위 JSON 붙여넣기
5. "변경 사항 저장"

### 1.3 CORS 설정

웹 브라우저에서 이미지 업로드를 허용하려면:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"]
  }
]
```

**적용 방법:**
1. S3 콘솔 > 버킷 선택
2. "권한" 탭
3. "CORS(Cross-origin 리소스 공유)" > "편집"
4. 위 JSON 붙여넣기
5. "변경 사항 저장"

---

## 🔐 2단계: IAM 권한 설정

### 2.1 IAM 정책 생성

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-ecommerce-images",
        "arn:aws:s3:::your-ecommerce-images/*"
      ]
    }
  ]
}
```

### 2.2 EC2 인스턴스 프로파일 연결

**방법 1: IAM Role 사용 (권장)**

```bash
# 1. IAM Role 생성
aws iam create-role --role-name EC2-S3-Access-Role \
  --assume-role-policy-document file://trust-policy.json

# 2. 정책 연결
aws iam put-role-policy --role-name EC2-S3-Access-Role \
  --policy-name S3-Access-Policy \
  --policy-document file://s3-policy.json

# 3. EC2에 Role 연결
aws ec2 associate-iam-instance-profile \
  --instance-id i-1234567890abcdef0 \
  --iam-instance-profile Name=EC2-S3-Access-Role
```

**방법 2: Access Key 사용 (비권장)**

```bash
# AWS CLI 설정
aws configure
# AWS Access Key ID: YOUR_ACCESS_KEY
# AWS Secret Access Key: YOUR_SECRET_KEY
# Default region name: ap-northeast-2
# Default output format: json
```

---

## 💻 3단계: 코드 구현

### 3.1 S3FileStorageService 완성

`product-service/src/main/java/com/ecommerce/product/service/S3FileStorageService.java`:

```java
package com.ecommerce.product.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.exception.ProductResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {
    
    private final S3Client s3Client;
    
    @Value("${file.storage.s3.bucket-name}")
    private String bucketName;
    
    @Value("${file.storage.s3.region:ap-northeast-2}")
    private String region;
    
    @Value("${file.storage.s3.max-file-size:5242880}") // 5MB
    private long maxFileSize;
    
    private static final List<String> ALLOWED_EXTENSIONS = 
        List.of("jpg", "jpeg", "png", "gif", "webp");
    
    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        
        // 고유한 파일명 생성
        String uniqueFileName = generateUniqueFileName(file);
        String key = directory + "/" + uniqueFileName;
        
        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // S3 URL 반환
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, key);
            
            log.info("File uploaded to S3 successfully: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", key, e);
            throw new BusinessException(ProductResultCode.IMAGE_UPLOAD_FAILED);
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3 successfully: {}", fileUrl);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking S3 object existence: {}", fileUrl, e);
            return false;
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ProductResultCode.IMAGE_SIZE_EXCEEDED);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ProductResultCode.INVALID_IMAGE_FORMAT);
        }
    }
    
    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString() + "." + extension;
    }
    
    private String extractKeyFromUrl(String fileUrl) {
        // https://bucket-name.s3.region.amazonaws.com/key 형식에서 key 추출
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        int secondLastSlashIndex = fileUrl.lastIndexOf('/', lastSlashIndex - 1);
        return fileUrl.substring(secondLastSlashIndex + 1);
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
```

### 3.2 S3Client Configuration

`product-service/src/main/java/com/ecommerce/product/config/S3Config.java`:

```java
package com.ecommerce.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "s3")
public class S3Config {
    
    @Value("${file.storage.s3.region:ap-northeast-2}")
    private String region;
    
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
```

### 3.3 Gradle 의존성 추가

`product-service/build.gradle`:

```gradle
dependencies {
    // 기존 의존성...
    
    // AWS S3 SDK
    implementation 'software.amazon.awssdk:s3:2.20.26'
}
```

---

## ⚙️ 4단계: 환경 설정

### 4.1 application.yml 수정

`general-service/src/main/resources/application.yml`:

```yaml
# 파일 스토리지 설정
file:
  storage:
    type: ${FILE_STORAGE_TYPE:local}  # local 또는 s3
    local:
      upload-dir: ${FILE_UPLOAD_DIR:./uploads}
    s3:
      bucket-name: ${S3_BUCKET_NAME:your-ecommerce-images}
      region: ${AWS_REGION:ap-northeast-2}
      max-file-size: 5242880  # 5MB
```

### 4.2 환경 변수 설정

**개발 환경 (로컬):**
```bash
# .env 파일 또는 환경 변수
FILE_STORAGE_TYPE=local
FILE_UPLOAD_DIR=./uploads
```

**운영 환경 (EC2):**
```bash
# /etc/systemd/system/tomcat.service
Environment="FILE_STORAGE_TYPE=s3"
Environment="S3_BUCKET_NAME=your-ecommerce-images"
Environment="AWS_REGION=ap-northeast-2"

# IAM Role 사용 시 Access Key 불필요
# Access Key 사용 시:
# Environment="AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY"
# Environment="AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY"
```

---

## 🔄 5단계: 기존 이미지 마이그레이션

### 5.1 로컬 파일을 S3로 복사

```bash
# AWS CLI로 일괄 업로드
aws s3 sync ./uploads/products s3://your-ecommerce-images/products/

# 또는 개별 파일 업로드
aws s3 cp ./uploads/products/image.jpg s3://your-ecommerce-images/products/image.jpg
```

### 5.2 데이터베이스 URL 업데이트

기존 이미지 URL을 S3 URL로 변경:

```sql
-- 로컬 URL을 S3 URL로 변경
UPDATE product_images 
SET image_url = REPLACE(
    image_url, 
    '/images/products/', 
    'https://your-ecommerce-images.s3.ap-northeast-2.amazonaws.com/products/'
)
WHERE image_url LIKE '/images/products/%';
```

---

## 🧪 6단계: 테스트

### 6.1 이미지 업로드 테스트

```bash
# Swagger UI에서 테스트
# 1. http://localhost:8080/swagger-ui.html 접속
# 2. POST /api/v1/admin/products/{productId}/images
# 3. 이미지 파일 선택 후 업로드
# 4. 응답에서 S3 URL 확인
```

### 6.2 이미지 조회 테스트

```bash
# 상품 목록 조회
curl http://localhost:8080/api/v1/products

# 응답 예시:
{
  "data": {
    "content": [
      {
        "productId": 1,
        "name": "상품명",
        "images": [
          {
            "imageUrl": "https://your-ecommerce-images.s3.ap-northeast-2.amazonaws.com/products/20260114_201057_uuid.png"
          }
        ]
      }
    ]
  }
}
```

### 6.3 이미지 삭제 테스트

```bash
# 상품 삭제 (이미지도 함께 삭제됨)
curl -X DELETE http://localhost:8080/api/v1/admin/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🎨 7단계: CloudFront CDN 연동 (선택사항)

### 7.1 CloudFront Distribution 생성

```bash
# AWS CLI로 생성
aws cloudfront create-distribution \
  --origin-domain-name your-ecommerce-images.s3.ap-northeast-2.amazonaws.com \
  --default-root-object index.html
```

### 7.2 application.yml 수정

```yaml
file:
  storage:
    type: s3
    s3:
      bucket-name: your-ecommerce-images
      region: ap-northeast-2
      cloudfront-domain: ${CLOUDFRONT_DOMAIN:}  # 예: d1234567890abc.cloudfront.net
```

### 7.3 S3FileStorageService 수정

```java
@Value("${file.storage.s3.cloudfront-domain:}")
private String cloudfrontDomain;

@Override
public String storeFile(MultipartFile file, String directory) throws IOException {
    // ... 업로드 로직 ...
    
    // CloudFront URL 반환 (설정된 경우)
    if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
        return String.format("https://%s/%s", cloudfrontDomain, key);
    }
    
    // S3 URL 반환
    return String.format("https://%s.s3.%s.amazonaws.com/%s", 
            bucketName, region, key);
}
```

---

## 📊 8단계: 모니터링

### 8.1 S3 메트릭 확인

```bash
# S3 버킷 크기 확인
aws s3 ls s3://your-ecommerce-images --recursive --summarize

# CloudWatch 메트릭 확인
aws cloudwatch get-metric-statistics \
  --namespace AWS/S3 \
  --metric-name BucketSizeBytes \
  --dimensions Name=BucketName,Value=your-ecommerce-images \
  --start-time 2026-01-01T00:00:00Z \
  --end-time 2026-01-14T23:59:59Z \
  --period 86400 \
  --statistics Average
```

### 8.2 비용 모니터링

```bash
# S3 비용 확인
aws ce get-cost-and-usage \
  --time-period Start=2026-01-01,End=2026-01-31 \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --filter file://s3-filter.json
```

---

## 🔧 트러블슈팅

### 1. Access Denied 에러

**증상:**
```
software.amazon.awssdk.services.s3.model.S3Exception: Access Denied
```

**해결:**
1. IAM 정책 확인
2. 버킷 정책 확인
3. EC2 인스턴스 프로파일 확인

### 2. 이미지 업로드 후 403 에러

**증상:** 이미지 URL 접근 시 403 Forbidden

**해결:**
1. 버킷 정책에서 Public Read 권한 확인
2. "퍼블릭 액세스 차단" 설정 해제

### 3. CORS 에러

**증상:** 브라우저에서 이미지 업로드 시 CORS 에러

**해결:**
1. S3 버킷 CORS 설정 확인
2. AllowedOrigins에 프론트엔드 도메인 추가

### 4. 느린 업로드 속도

**해결:**
1. CloudFront CDN 사용
2. S3 Transfer Acceleration 활성화
3. 이미지 압축 적용

---

## 💰 비용 최적화

### 1. S3 Intelligent-Tiering

자주 접근하지 않는 이미지를 자동으로 저렴한 스토리지로 이동:

```bash
aws s3api put-bucket-intelligent-tiering-configuration \
  --bucket your-ecommerce-images \
  --id auto-archive \
  --intelligent-tiering-configuration file://tiering-config.json
```

### 2. Lifecycle Policy

오래된 이미지 자동 삭제:

```json
{
  "Rules": [
    {
      "Id": "DeleteOldImages",
      "Status": "Enabled",
      "Prefix": "temp/",
      "Expiration": {
        "Days": 30
      }
    }
  ]
}
```

### 3. 이미지 최적화

업로드 전 이미지 압축:

```java
// ImageOptimizer.java
public MultipartFile optimizeImage(MultipartFile file) {
    // WebP 변환, 리사이징 등
}
```

---

## ✅ 체크리스트

마이그레이션 전 확인사항:

- [ ] S3 버킷 생성 완료
- [ ] IAM 권한 설정 완료
- [ ] 버킷 정책 설정 완료 (Public Read)
- [ ] CORS 설정 완료
- [ ] Gradle 의존성 추가 완료
- [ ] S3FileStorageService 구현 완료
- [ ] S3Config 구현 완료
- [ ] application.yml 설정 완료
- [ ] 환경 변수 설정 완료
- [ ] 기존 이미지 S3 업로드 완료
- [ ] 데이터베이스 URL 업데이트 완료
- [ ] 이미지 업로드 테스트 완료
- [ ] 이미지 조회 테스트 완료
- [ ] 이미지 삭제 테스트 완료

---

## 📚 참고 자료

- [AWS S3 공식 문서](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [S3 버킷 정책 예제](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html)
- [CloudFront 설정 가이드](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/)

---

## 🎯 요약

1. **S3 버킷 생성** + 권한 설정
2. **IAM Role** 또는 Access Key 설정
3. **코드 구현**: S3FileStorageService + S3Config
4. **환경 설정**: application.yml + 환경 변수
5. **기존 이미지 마이그레이션**: AWS CLI로 업로드 + DB 업데이트
6. **테스트**: 업로드/조회/삭제 확인
7. **(선택) CloudFront CDN** 연동

완료!
