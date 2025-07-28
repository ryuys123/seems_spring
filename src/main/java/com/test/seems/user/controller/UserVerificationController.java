package com.test.seems.user.controller;

import com.test.seems.user.model.dto.UserVerificationRequest;
import com.test.seems.user.model.dto.UserVerificationResponse;
import com.test.seems.user.model.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user/verification")
@RequiredArgsConstructor
public class UserVerificationController {

    private final UserVerificationService userVerificationService;

    // 본인인증 통합 엔드포인트
    @PostMapping
    public ResponseEntity<UserVerificationResponse> verification(@RequestBody UserVerificationRequest request) {
        log.info("본인인증 요청: type={}, phone={}", request.getVerificationType(), request.getPhone());
        log.info("전체 요청 데이터: {}", request);

        UserVerificationResponse response = userVerificationService.processVerification(request);
        
        log.info("응답 데이터: {}", response);

        return ResponseEntity.ok(response);
    }
}
