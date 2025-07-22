package com.test.seems.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDeleteRequest {
    private String userType;  // "normal" 또는 "social"
    private String password;  // 일반 로그인 사용자의 비밀번호 (소셜 로그인 시 null)
    private String socialType; // "kakao", "naver", "google" (소셜 로그인 시)
}
