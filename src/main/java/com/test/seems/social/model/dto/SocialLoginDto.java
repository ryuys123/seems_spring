package com.test.seems.social.model.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialLoginDto {
    private Long socialLoginId;
    private String userId;      // UserEntity의 userId
    private String provider;    // GOOGLE, KAKAO 등
    private String socialId;    // 소셜 플랫폼 고유 ID
    private String socialEmail; // 소셜 계정의 이메일
    private String linkedAt;    // ISO String 등으로 변환해서 사용
} 