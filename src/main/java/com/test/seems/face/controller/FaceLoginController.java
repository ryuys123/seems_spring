package com.test.seems.face.controller;

import com.test.seems.face.jpa.entity.FaceLoginEntity;
import com.test.seems.face.model.dto.FaceLoginRequest;
import com.test.seems.face.model.dto.FaceLoginResponse;
import com.test.seems.face.model.dto.FaceRegistrationRequest;
import com.test.seems.face.model.dto.FaceRegistrationResponse;
import com.test.seems.face.model.service.FaceLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceLoginController {
    
    private final FaceLoginService faceLoginService;
    
    /**
     * 페이스 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<FaceLoginResponse> login(@RequestBody FaceLoginRequest request) {
        log.info("페이스 로그인 요청");
        
        FaceLoginResponse response = faceLoginService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 페이스 등록
     */
    @PostMapping("/register")
    public ResponseEntity<FaceRegistrationResponse> registerFace(@RequestBody FaceRegistrationRequest request) {
        log.info("페이스 등록 요청: 사용자 {}", request.getUserId());
        
        FaceRegistrationResponse response = faceLoginService.registerFace(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 페이스 삭제
     */
    @DeleteMapping("/{userId}/{faceName}")
    public ResponseEntity<String> deleteFace(@PathVariable String userId, @PathVariable String faceName) {
        log.info("페이스 삭제 요청: 사용자 {}, 페이스 {}", userId, faceName);
        
        boolean success = faceLoginService.deleteFace(userId, faceName);
        
        if (success) {
            return ResponseEntity.ok("페이스 삭제 성공");
        } else {
            return ResponseEntity.badRequest().body("페이스 삭제 실패");
        }
    }
    
    /**
     * 사용자의 등록된 페이스 목록 조회
     */
    @GetMapping("/{userId}/faces")
    public ResponseEntity<List<FaceLoginEntity>> getUserFaces(@PathVariable String userId) {
        log.info("사용자 페이스 목록 조회: 사용자 {}", userId);
        
        List<FaceLoginEntity> faces = faceLoginService.getUserFaces(userId);
        return ResponseEntity.ok(faces);
    }
    
    /**
     * 페이스 로그인 활성화 상태 확인
     */
    @GetMapping("/{userId}/enabled")
    public ResponseEntity<Boolean> isFaceLoginEnabled(@PathVariable String userId) {
        log.info("페이스 로그인 활성화 상태 확인: 사용자 {}", userId);
        
        boolean enabled = faceLoginService.isFaceLoginEnabled(userId);
        return ResponseEntity.ok(enabled);
    }
    
    /**
     * 페이스 로그인 활성화/비활성화
     */
    @PutMapping("/{userId}/toggle")
    public ResponseEntity<String> toggleFaceLogin(@PathVariable String userId, @RequestParam boolean enabled) {
        log.info("페이스 로그인 상태 변경: 사용자 {}, 활성화: {}", userId, enabled);
        
        boolean success = faceLoginService.toggleFaceLogin(userId, enabled);
        
        if (success) {
            String message = enabled ? "페이스 로그인이 활성화되었습니다." : "페이스 로그인이 비활성화되었습니다.";
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body("페이스 로그인 상태 변경 실패");
        }
    }
}