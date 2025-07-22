package com.test.seems.user.model.dto;

import lombok.Data;

@Data
public class SmsVerificationRequest {
    private String phone;
    private String verificationCode; // 문자 발송 시에는 null, 인증 시에는 값 입력
}
