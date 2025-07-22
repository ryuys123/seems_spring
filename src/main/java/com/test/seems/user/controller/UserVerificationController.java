package com.test.seems.user.controller;

import com.test.seems.user.model.dto.*;
import com.test.seems.user.model.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/user/verification")
@RequiredArgsConstructor
public class UserVerificationController {
    private final UserVerificationService userVerificationService;

    // 아이디 찾기
    @PostMapping("/find-id")
    public FindIdResponse findId(@RequestBody FindIdRequest request) {
        return userVerificationService.findId(request);
    }

    // 비밀번호 찾기(재설정)
    @PostMapping("/find-password")
    public FindPasswordResponse findPassword(@RequestBody FindPasswordRequest request) {
        return userVerificationService.findPassword(request);
    }

    // 문자 인증(발송/검증 통합)
    @PostMapping("/sms-verification")
    public SmsVerificationResponse smsVerification(@RequestBody SmsVerificationRequest request) {
        return userVerificationService.smsVerification(request);
    }

}
