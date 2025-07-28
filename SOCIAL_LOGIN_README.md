# 소셜 로그인 시스템 개선

## 🎯 개선 목적

1. **social_id와 social_email 분리**: 소셜 플랫폼의 고유 ID와 이메일을 별도로 관리
2. **이메일 인증 확장성**: 향후 이메일 인증, 중복 체크, 마케팅 등 다양한 용도로 활용
3. **데이터 무결성**: 소셜 플랫폼의 고유 ID는 변경되지 않으므로 안정적인 로그인 보장

## 📊 데이터베이스 구조

### TB_USER_SOCIAL_LOGIN 테이블

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| SOCIAL_LOGIN_ID | NUMBER(19) | 소셜 로그인 고유 식별자 (PK) |
| USER_ID | VARCHAR2(255) | TB_USERS 테이블의 사용자 ID (FK) |
| PROVIDER | VARCHAR2(50) | 소셜 로그인 제공자 (GOOGLE, KAKAO, NAVER) |
| SOCIAL_ID | VARCHAR2(100) | **소셜 플랫폼 고유 ID** (변경 불가, 로그인용) |
| SOCIAL_EMAIL | VARCHAR2(100) | **소셜 계정의 이메일** (이메일 인증, 중복 체크용) |
| LINKED_AT | DATE | 소셜 로그인 계정 연동 시간 |

## 🔧 주요 변경사항

### 1. 엔티티 수정
- `SocialLoginEntity`에 `socialEmail` 필드 추가
- `SocialUserDto`에 `socialEmail` 필드 추가
- `SocialLoginDto`에 `socialEmail` 필드 추가
- `SocialSignupCompleteDto`에 `socialEmail` 필드 추가

### 2. Repository 확장
```java
// 기존 메서드
Optional<SocialLoginEntity> findByProviderAndSocialId(String provider, String socialId);

// 신규 메서드
Optional<SocialLoginEntity> findBySocialEmail(String socialEmail);
Optional<SocialLoginEntity> findByProviderAndSocialEmail(String provider, String socialEmail);
List<SocialLoginEntity> findByUser_UserId(String userId);
```

### 3. Service 로직 개선
- `SocialLoginService.registerSocialUser()` 메서드에 `socialEmail` 매개변수 추가
- `SocialOAuthService.socialLoginOrSignup()` 메서드에서 `socialEmail` 저장

### 4. Controller 수정
- 소셜 로그인 콜백에서 `socialEmail` 정보 전달
- 회원가입 완료 시 `socialEmail` 저장

## 🚀 사용 시나리오

### 1. 소셜 로그인/연동
```java
// provider + social_id(고유 ID)로 조회
Optional<SocialLoginEntity> socialLogin = socialLoginRepository
    .findByProviderAndSocialId("KAKAO", "123456789");
```

### 2. 이메일 중복 체크
```java
// social_email로 조회
Optional<SocialLoginEntity> socialLogin = socialLoginRepository
    .findBySocialEmail("user@example.com");
```

### 3. 특정 사용자의 모든 소셜 연동 정보
```java
// 사용자의 모든 소셜 연동 정보 조회
List<SocialLoginEntity> socialLogins = socialLoginRepository
    .findByUser_UserId("user123");
```

## 📝 마이그레이션

### 1. 데이터베이스 스크립트 실행
```sql
-- dbscript/social_email_migration.sql 실행
-- 기존 데이터에 대해 social_email 컬럼 추가 및 업데이트
```

### 2. 애플리케이션 재시작
```bash
./gradlew clean build
```

## 🔍 테스트 방법

### 1. 소셜 로그인 테스트
1. 카카오/구글/네이버 로그인 시도
2. 신규 사용자: 추가 정보 입력 페이지 확인
3. 기존 사용자: 바로 대시보드로 이동 확인

### 2. 데이터 저장 확인
```sql
-- 소셜 로그인 정보 확인
SELECT 
    USER_ID,
    PROVIDER,
    SOCIAL_ID,
    SOCIAL_EMAIL,
    LINKED_AT
FROM TB_USER_SOCIAL_LOGIN
ORDER BY LINKED_AT DESC;
```

## ⚠️ 주의사항

1. **카카오 로그인**: 이메일 제공 동의가 필요할 수 있음
2. **기존 데이터**: 마이그레이션 스크립트로 기존 데이터 처리
3. **이메일 중복**: 동일한 이메일로 여러 소셜 계정 연동 가능성 고려

## 🔮 향후 확장 계획

1. **이메일 인증**: `social_email`을 활용한 이메일 인증 시스템
2. **계정 통합**: 동일한 이메일로 여러 소셜 계정 연동
3. **마케팅**: `social_email`을 활용한 이메일 마케팅
4. **보안 강화**: 이메일 변경 시 인증 프로세스

## 📞 문의사항

소셜 로그인 관련 문의사항이 있으시면 개발팀에 연락해주세요. 