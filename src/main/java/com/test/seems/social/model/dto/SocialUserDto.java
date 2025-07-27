package com.test.seems.social.model.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialUserDto {
    private String provider;      // "KAKAO", "NAVER", "GOOGLE"
    private String socialId;      // 소셜 플랫폼 고유 ID
    private String socialEmail;   // 소셜 계정의 이메일
    private String email;         // 사용자 이메일 (회원가입용)
    private String name;
    private String profileImage;
    // 아래는 회원가입 시만 사용, 로그인 시에는 null 가능
    private String userId;
    private String password;
    private String phone;
} 