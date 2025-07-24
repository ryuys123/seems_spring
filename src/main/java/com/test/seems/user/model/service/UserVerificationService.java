package com.test.seems.user.model.service;

import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import com.test.seems.user.model.dto.UserVerificationRequest;
import com.test.seems.user.model.dto.UserVerificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // 인증번호 임시 저장소 (실제 서비스는 Redis 사용 권장)
    private final Map<String, String> verificationCodeStore = new ConcurrentHashMap<>();

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.sender}")
    private String sender;



    // 본인인증 통합 처리
    public UserVerificationResponse processVerification(UserVerificationRequest request) {
        try {
            switch (request.getVerificationType()) {
                case "SMS_SEND":
                    return sendSmsVerification(request);
                case "SMS_VERIFY":
                    return verifySmsCode(request);
                case "FIND_ID":
                    return findUserId(request);
                case "FIND_PASSWORD":
                    return resetPassword(request);
                default:
                    UserVerificationResponse response = new UserVerificationResponse();
                    response.setSuccess(false);
                    response.setMessage("잘못된 인증 타입입니다.");
                    return response;
            }
        } catch (Exception e) {
            log.error("본인인증 처리 중 오류: {}", e.getMessage());
            UserVerificationResponse response = new UserVerificationResponse();
            response.setSuccess(false);
            response.setMessage("처리 중 오류가 발생했습니다.");
            return response;
        }
    }

    // 문자 인증번호 발송
    private UserVerificationResponse sendSmsVerification(UserVerificationRequest request) {
        String code = generateRandomCode();
        verificationCodeStore.put(request.getPhone(), code);

        boolean sent = sendSms(request.getPhone(), code);

        UserVerificationResponse response = new UserVerificationResponse();
        response.setSuccess(sent);
        response.setMessage(sent ? "인증번호가 발송되었습니다." : "문자 발송에 실패했습니다.");
        return response;
    }

    // 문자 인증번호 검증
    private UserVerificationResponse verifySmsCode(UserVerificationRequest request) {
        String storedCode = verificationCodeStore.get(request.getPhone());
        boolean success = storedCode != null && storedCode.equals(request.getVerificationCode());

        UserVerificationResponse response = new UserVerificationResponse();
        response.setSuccess(success);
        response.setMessage(success ? "인증이 완료되었습니다." : "인증번호가 일치하지 않습니다.");
        return response;
    }

    // 아이디 찾기
    private UserVerificationResponse findUserId(UserVerificationRequest request) {
        // 이름과 휴대폰번호로 사용자 조회
        UserEntity user = userRepository.findByUserNameAndPhone(request.getName(), request.getPhone());

        UserVerificationResponse response = new UserVerificationResponse();
        if (user == null) {
            response.setSuccess(false);
            response.setMessage("일치하는 사용자가 없습니다.");
        } else {
            response.setSuccess(true);
            response.setMessage("아이디를 찾았습니다.");
            response.setFoundUserId(user.getUserId());
            response.setFoundUsername(user.getUserName());
        }
        return response;
    }

    // 비밀번호 재설정
    private UserVerificationResponse resetPassword(UserVerificationRequest request) {
        // 인증번호 검증
        String storedCode = verificationCodeStore.get(request.getPhone());
        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            UserVerificationResponse response = new UserVerificationResponse();
            response.setSuccess(false);
            response.setMessage("인증번호가 일치하지 않습니다.");
            return response;
        }

        // 사용자 조회 및 비밀번호 변경
        UserEntity user = userRepository.findByUserId(request.getUserId());
        if (user == null) {
            UserVerificationResponse response = new UserVerificationResponse();
            response.setSuccess(false);
            response.setMessage("사용자를 찾을 수 없습니다.");
            return response;
        }

        user.setUserPwd(request.getNewPassword()); // 실제로는 암호화 필요
        userRepository.save(user);

        UserVerificationResponse response = new UserVerificationResponse();
        response.setSuccess(true);
        response.setMessage("비밀번호가 재설정되었습니다.");
        return response;
    }

    // Coolsms API로 문자 발송 (공식 SDK 사용)
    private boolean sendSms(String phone, String code) {
        try {
            Message message = new Message();
            message.setFrom(sender);
            message.setTo(phone);
            message.setText("[SEEMS] 인증번호: " + code);

            messageService.send(message);
            return true;
        } catch (Exception e) {
            log.error("문자 발송 실패: {}", e.getMessage());
            return false;
        }
    }


    // 6자리 랜덤 인증번호 생성
    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
