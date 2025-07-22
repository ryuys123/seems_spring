package com.test.seems.user.model.service;

import org.springframework.stereotype.Service;

@Service
public class SolapiSmsService {
    private final String apikey = "NCSGHKHLWAV10S7Z";
    private final String apisecret = "MB9E3SILHNOF0OBZ0NBDW1JT2RHDB73K";
    private final String senderPhone = "01028258251";   // 발신번호 (승인된 번호)

    public boolean sendSms(String phone, String code) {
        // 실제로는 솔라피 API 연동
        System.out.println("문자 발송: " + phone + " / 인증번호: " + code);
        return true; // 성공 가정
    }
}
