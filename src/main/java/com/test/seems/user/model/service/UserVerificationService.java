package com.test.seems.user.model.service;

import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import com.test.seems.user.model.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserVerificationService {

    private final UserRepository userRepository;
    private final SolapiSmsService solapiSmsService;
    // 인증번호 임시 저장소(빠른 구현용, 실제 서비스는 Redis 등 사용 권장)
    private final Map<String, String> verificationCodeStore = new ConcurrentHashMap<>();

    public UserVerificationService(UserRepository userRepository, SolapiSmsService solapiSmsService) {
        this.userRepository = userRepository;
        this.solapiSmsService = solapiSmsService;
    }

    // 아이디 찾기
    public FindIdResponse findId(FindIdRequest request) {
        // phone/email로 사용자 조회
        UserEntity user = userRepository.findByPhone(request.getPhone());
        if (user == null) {
            return new FindIdResponse(null, null, "일치하는 사용자가 없습니다.");
        }
        return new FindIdResponse(user.getUserId(), user.getUserName(), "아이디를 찾았습니다.");
    }

    // 비밀번호 찾기(재설정)
    public FindPasswordResponse findPassword(FindPasswordRequest request) {
        // 인증번호 검증
        String code = verificationCodeStore.get(request.getPhone());
        if (code == null || !code.equals(request.getVerificationCode())) {
            return new FindPasswordResponse(false, "인증번호가 일치하지 않습니다.");
        }
        // 비밀번호 재설정
        UserEntity user = userRepository.findByUserId(request.getUserId());
        if (user == null) {
            return new FindPasswordResponse(false, "사용자를 찾을 수 없습니다.");
        }
        user.setUserPwd(request.getNewPassword()); // 실제로는 암호화 필요
        userRepository.save(user);
        return new FindPasswordResponse(true, "비밀번호가 재설정되었습니다.");
    }

    // 문자 인증(발송/검증 통합)
    public SmsVerificationResponse smsVerification(SmsVerificationRequest request) {
        if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
            // 문자 발송
            String code = generateRandomCode();
            verificationCodeStore.put(request.getPhone(), code);
            boolean sent = solapiSmsService.sendSms(request.getPhone(), code);
            return new SmsVerificationResponse(sent, sent ? "인증번호가 발송되었습니다." : "문자 발송 실패");
        } else {
            // 인증번호 검증
            String code = verificationCodeStore.get(request.getPhone());
            boolean success = code != null && code.equals(request.getVerificationCode());
            return new SmsVerificationResponse(success, success ? "인증 성공" : "인증 실패");
        }
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
    }



}
