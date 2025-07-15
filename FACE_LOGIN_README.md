# 페이스 로그인 기능 구현

## 개요
Spring Boot 백엔드와 Python DeepFace를 연동한 페이스 로그인 기능입니다. 사용자는 얼굴 인식을 통해 로그인할 수 있으며, 기존 자체 로그인과 분리되어 관리됩니다.

## 구조

### 백엔드 (Spring Boot)
```
src/main/java/com/test/seems/face/
├── controller/
│   └── FaceLoginController.java      # 페이스 로그인 API 컨트롤러
├── model/
│   ├── dto/
│   │   ├── FaceLoginRequest.java     # 페이스 로그인 요청 DTO
│   │   └── FaceLoginResponse.java    # 페이스 로그인 응답 DTO
│   └── service/
│       └── FaceLoginService.java     # 페이스 로그인 비즈니스 로직
├── jpa/
│   ├── entity/
│   │   └── FaceLoginEntity.java      # 페이스 로그인 엔티티
│   └── repository/
│       └── FaceLoginRepository.java  # 페이스 로그인 리포지토리
├── util/
│   ├── FaceRecognitionUtil.java      # 얼굴인식 유틸리티
│   └── DeepFaceClient.java          # Python DeepFace 연동 클라이언트
├── filter/
│   └── FaceLoginFilter.java         # 페이스 로그인 전용 필터
├── config/
│   └── FaceConfig.java              # 페이스 로그인 설정
└── exception/
    └── FaceLoginException.java       # 페이스 로그인 예외
```

### Python DeepFace 서비스
```
python_face_service/
├── app.py                           # Flask DeepFace API 서버
└── requirements.txt                 # Python 의존성
```

## API 엔드포인트

### 1. 페이스 로그인
```
POST /face/login
Content-Type: application/json

{
  "imageData": "base64_encoded_image_data"
}
```

**응답:**
```json
{
  "accessToken": "jwt_access_token",
  "refreshToken": "jwt_refresh_token",
  "userId": "user123",
  "userName": "홍길동",
  "authType": "FACE",
  "message": "페이스로그인 성공"
}
```

### 2. 페이스 등록
```
POST /face/register
Content-Type: application/json

{
  "imageData": "base64_encoded_image_data",
  "userId": "user123",
  "phone": "010-1234-5678"
}
```

**응답:**
```json
{
  "faceLoginId": 1,
  "registeredAt": "2024-01-01T12:00:00",
  "message": "페이스 등록 성공"
}
```

### 3. 페이스 로그인 상태 조회
```
GET /face/status/{userId}
```

**응답:**
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "isFaceLoginEnabled": true,
  "lastUsedAt": "2024-01-01T12:00:00",
  "usageCount": 5,
  "status": "ACTIVE"
}
```

### 4. 페이스 로그인 활성화/비활성화
```
PUT /face/status/{userId}
Content-Type: application/json

{
  "isActive": true
}
```

**응답:**
```json
{
  "message": "페이스로그인이 활성화되었습니다."
}
```

## 설정

### application.properties
```properties
# 페이스 로그인 설정
face.model-name=VGG-Face
face.distance-metric=cosine
face.similarity-threshold=0.6
face.python-service-url=http://localhost:5000
face.face-recognition-endpoint=/api/face/recognize
face.face-feature-endpoint=/api/face/extract
face.max-image-size=1048576
face.enable-face-login=true
face.max-login-attempts=5
face.lockout-duration=300
```

## 설치 및 실행

### 1. Python DeepFace 서비스 실행
```bash
cd python_face_service
pip install -r requirements.txt
python app.py
```

### 2. Spring Boot 애플리케이션 실행
```bash
./gradlew bootRun
```

## 사용 방법

### 1. 페이스 등록
1. 사용자가 자체 로그인으로 로그인
2. 페이스 등록 페이지에서 얼굴 사진 촬영
3. 전화번호 확인 후 페이스 등록 완료

### 2. 페이스 로그인
1. 로그인 페이지에서 "페이스 로그인" 선택
2. 얼굴 사진 촬영
3. DeepFace AI가 얼굴을 인식하여 사용자 확인
4. JWT 토큰 발급 및 로그인 완료

## 보안 기능

1. **토큰 분리**: 페이스 로그인과 자체 로그인의 토큰을 분리하여 관리
2. **필터 분리**: 페이스 로그인 전용 필터로 보안 강화
3. **활성화/비활성화**: 사용자가 페이스 로그인을 언제든지 비활성화 가능
4. **사용 기록**: 페이스 로그인 사용 횟수와 마지막 사용 시간 추적

## 오류 처리

### 주요 예외 상황
- 얼굴인식 실패
- 등록되지 않은 사용자
- 비활성화된 페이스 로그인
- 이미지 형식 오류
- Python 서비스 연결 실패

### 오류 응답 형식
```json
{
  "error": "오류 메시지",
  "errorCode": "ERROR_CODE"
}
```

## 개발 환경

- **Spring Boot**: 3.x
- **Java**: 17+
- **Python**: 3.8+
- **DeepFace**: 0.0.79
- **Flask**: 2.3.3
- **OpenCV**: 4.8.1.78

## 주의사항

1. **Python 서비스**: DeepFace 서비스가 실행 중이어야 함
2. **이미지 크기**: 최대 1MB까지 지원
3. **이미지 형식**: JPG, JPEG, PNG 지원
4. **네트워크**: Python 서비스와 Spring Boot 간 통신 필요
5. **보안**: 실제 운영 환경에서는 HTTPS 사용 권장

## 테스트

### 1. Python 서비스 테스트
```bash
curl -X GET http://localhost:5000/health
```

### 2. 페이스 로그인 테스트
```bash
# 페이스 등록
curl -X POST http://localhost:8888/seems/face/register \
  -H "Content-Type: application/json" \
  -d '{
    "imageData": "base64_image_data",
    "userId": "testuser",
    "phone": "010-1234-5678"
  }'

# 페이스 로그인
curl -X POST http://localhost:8888/seems/face/login \
  -H "Content-Type: application/json" \
  -d '{
    "imageData": "base64_image_data"
  }'
``` 