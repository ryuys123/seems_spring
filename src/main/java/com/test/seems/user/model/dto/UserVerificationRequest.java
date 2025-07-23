package com.test.seems.user.model.dto;

import lombok.Data;

@Data
public class UserVerificationRequest {
    // 요청용 필드만 유지
    private String phone;              // 전화번호
    private String userId;             // 비밀번호 찾기용
    private String name;              // 아이디 찾기용 이름
    private String verificationCode;   // 문자 인증번호
    private String newPassword;        // 비밀번호 재설정용
    private String verificationType;   // "FIND_ID", "FIND_PASSWORD", "SMS_SEND", "SMS_VERIFY"
} 