package com.test.seems.social.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSignupCompleteDto {
    private String userId;        // 사용자 아이디
    private String userPwd;       // 비밀번호
    private String userName;      // 사용자 이름
    private String phone;         // 전화번호
    private String email;         // 이메일
    private String provider;      // 소셜 로그인 제공자 (google, kakao, naver)
    private String socialId;      // 소셜 로그인 ID
    private String socialEmail;   // 소셜 계정의 이메일
    private String sessionId;     // 세션 ID (세션 기반 인증용)
    private String profileImage;  // 프로필 이미지 (base64 또는 URL)
} 