package com.test.seems.user.model.dto;

import lombok.Data;

@Data
public class UserVerificationResponse {
    private boolean success;
    private String message;
    private String foundUserId;        // 아이디 찾기 결과
    private String foundUsername;      // 아이디 찾기 결과
} 