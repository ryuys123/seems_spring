package com.test.seems.user.model.dto;

import lombok.Data;

@Data
public class FindPasswordRequest {
    private String userId;
    private String phone; // 또는 email
    private String verificationCode; // 문자 인증번호
    private String newPassword;      // 비밀번호 재설정 시
}
