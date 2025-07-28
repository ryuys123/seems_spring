package com.test.seems.face.controller;

import com.test.seems.face.jpa.entity.FaceLoginEntity;
import com.test.seems.face.model.dto.FaceLoginRequest;
import com.test.seems.face.model.dto.FaceLoginResponse;
import com.test.seems.face.model.dto.FaceRegistrationRequest;
import com.test.seems.face.model.dto.FaceRegistrationResponse;
import com.test.seems.face.model.dto.FaceSignupRequest;
import com.test.seems.face.model.dto.FaceSignupResponse;
import com.test.seems.face.model.service.FaceLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<FaceLoginResponse> faceLogin(@RequestBody FaceLoginRequest request) {
        log.info("페이스 로그인 요청: 얼굴 인식 시작");
        log.info("요청 데이터 - faceImageData 길이: {}, faceName: {}",
                request.getFaceImageData() != null ? request.getFaceImageData().length() : "null",
                request.getFaceName());

        FaceLoginResponse response = faceLoginService.faceLogin(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 페이스 회원가입 (사용자 생성 + 페이스 등록)
     */
    @PostMapping("/signup")
    public ResponseEntity<FaceSignupResponse> faceSignup(@RequestBody FaceSignupRequest request) {
        log.info("페이스 회원가입 요청 수신: 전체 요청 데이터={}", request);
        log.info("페이스 회원가입 요청 상세: userId={}, username={}, phone={}, password={}, faceImageData 길이={}, imageData 길이={}", 
                request.getUserId(), 
                request.getUsername(), 
                request.getPhone(), 
                request.getPassword() != null ? "설정됨" : "null",
                request.getFaceImageData() != null ? request.getFaceImageData().length() : "null",
                request.getImageData() != null ? request.getImageData().length() : "null");

        FaceSignupResponse response = faceLoginService.faceSignup(request);
        
        log.info("페이스 회원가입 응답: success={}, message={}", response.isSuccess(), response.getMessage());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 페이스 등록 (기존 사용자에게 페이스 추가)
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
     * 기존 사용자 페이스 연동 (비밀번호 불필요)
     */
    @PostMapping("/link")
    public ResponseEntity<Map<String, Object>> linkFaceToExistingUser(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String faceImageData = request.get("faceImageData");
        String faceName = request.get("faceName");
        
        log.info("기존 사용자 페이스 연동 요청: userId={}, faceName={}, faceImageData 길이={}", 
                userId, faceName, faceImageData != null ? faceImageData.length() : "null");

        try {
            // 입력 검증
            if (userId == null || userId.trim().isEmpty()) {
                log.error("페이스 연동 실패: userId가 null이거나 비어있음");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "사용자 ID는 필수입니다."
                ));
            }
            
            if (faceImageData == null || faceImageData.trim().isEmpty()) {
                log.error("페이스 연동 실패: faceImageData가 null이거나 비어있음");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "얼굴 이미지는 필수입니다."
                ));
            }

            // FaceRegistrationRequest로 변환하여 기존 서비스 사용
            FaceRegistrationRequest registrationRequest = new FaceRegistrationRequest();
            registrationRequest.setUserId(userId);
            registrationRequest.setFaceName(faceName != null ? faceName : "default");
            registrationRequest.setFaceImageData(faceImageData);

            FaceRegistrationResponse response = faceLoginService.registerFace(registrationRequest);

            if (response.isSuccess()) {
                log.info("기존 사용자 페이스 연동 성공: userId={}", userId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "페이스 연동이 완료되었습니다.",
                    "userId", userId,
                    "faceName", faceName != null ? faceName : "default"
                ));
            } else {
                log.error("기존 사용자 페이스 연동 실패: userId={}, message={}", userId, response.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", response.getMessage()
                ));
            }
        } catch (Exception e) {
            log.error("기존 사용자 페이스 연동 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "페이스 연동 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
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

    // 페이스 등록 여부 확인 API (기존)
    @GetMapping("/user/face-status")
    public Map<String, Boolean> getFaceStatus(@RequestParam String userId) {
        boolean hasFace = faceLoginService.isFaceLoginEnabled(userId);
        return Map.of("hasFace", hasFace);
    }

    // 프론트엔드 요구사항 - 페이스 연동 상태 확인 API
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFaceLinkStatus(@RequestParam String userId) {
        log.info("페이스 연동 상태 확인: userId={}", userId);
        
        try {
            // 페이스 로그인 활성화 상태 확인
            boolean isEnabled = faceLoginService.isFaceLoginEnabled(userId);
            
            // 등록된 페이스 목록 조회
            List<FaceLoginEntity> faces = faceLoginService.getUserFaces(userId);
            boolean hasRegisteredFaces = !faces.stream().filter(FaceLoginEntity::isActive).toList().isEmpty();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "userId", userId,
                "faceLinked", isEnabled && hasRegisteredFaces,  // 프론트엔드 요구사항
                "faceLoginEnabled", isEnabled,
                "hasRegisteredFaces", hasRegisteredFaces,
                "registeredFaces", faces.stream()
                    .filter(FaceLoginEntity::isActive)
                    .map(face -> Map.of(
                        "faceName", face.getFaceName(),
                        "registeredAt", face.getRegisteredAt(),
                        "lastUsedAt", face.getLastUsedAt()
                    ))
                    .toList()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("페이스 연동 상태 확인 중 오류: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "페이스 연동 상태 확인 실패"
            ));
        }
    }

    // 페이스 연동 해제(삭제) API
    @DeleteMapping("/face/unlink")
    public ResponseEntity<?> unlinkFace(@RequestParam String userId, @RequestParam String faceName) {
        boolean result = faceLoginService.deleteFace(userId, faceName);
        if (result) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "페이스 연동 해제 실패"));
        }
    }

    /**
     * 마이페이지용 - 사용자의 페이스 연동 상태 조회
     */
    @GetMapping("/user/status")
    public ResponseEntity<Map<String, Object>> getUserFaceStatus(@RequestParam String userId) {
        log.info("사용자 페이스 연동 상태 조회: {}", userId);
        
        try {
            // 페이스 로그인 활성화 상태 확인
            boolean isEnabled = faceLoginService.isFaceLoginEnabled(userId);
            
            // 등록된 페이스 목록 조회
            List<FaceLoginEntity> faces = faceLoginService.getUserFaces(userId);
            
            Map<String, Object> response = Map.of(
                "userId", userId,
                "faceLoginEnabled", isEnabled,
                "registeredFaces", faces.stream()
                    .filter(FaceLoginEntity::isActive)
                    .map(face -> Map.of(
                        "faceName", face.getFaceName(),
                        "registeredAt", face.getRegisteredAt(),
                        "lastUsedAt", face.getLastUsedAt()
                    ))
                    .toList(),
                "hasRegisteredFaces", !faces.stream().filter(FaceLoginEntity::isActive).toList().isEmpty()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("페이스 연동 상태 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "페이스 연동 상태 조회 실패"
            ));
        }
    }

    /**
     * 마이페이지용 - 페이스 등록 (기존 사용자)
     */
    @PostMapping("/user/register")
    public ResponseEntity<Map<String, Object>> registerFaceForUser(@RequestBody FaceRegistrationRequest request) {
        log.info("마이페이지 페이스 등록 요청: 사용자 {}", request.getUserId());
        
        try {
            FaceRegistrationResponse response = faceLoginService.registerFace(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "페이스 등록이 완료되었습니다.",
                    "faceName", request.getFaceName()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", response.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("페이스 등록 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "페이스 등록 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 마이페이지용 - 페이스 연동 해제
     */
    @DeleteMapping("/user/unlink")
    public ResponseEntity<Map<String, Object>> unlinkFaceForUser(
            @RequestParam String userId, 
            @RequestParam String faceName) {
        log.info("마이페이지 페이스 연동 해제 요청: 사용자 {}, 페이스 {}", userId, faceName);
        
        try {
            boolean result = faceLoginService.deleteFace(userId, faceName);
            
            if (result) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "페이스 연동이 해제되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "페이스 연동 해제에 실패했습니다."
                ));
            }
            
        } catch (Exception e) {
            log.error("페이스 연동 해제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "페이스 연동 해제 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 마이페이지용 - 페이스 로그인 활성화/비활성화
     */
    @PutMapping("/user/toggle")
    public ResponseEntity<Map<String, Object>> toggleFaceLoginForUser(
            @RequestParam String userId, 
            @RequestParam boolean enabled) {
        log.info("마이페이지 페이스 로그인 상태 변경: 사용자 {}, 활성화: {}", userId, enabled);
        
        try {
            boolean result = faceLoginService.toggleFaceLogin(userId, enabled);
            
            if (result) {
                String message = enabled ? "페이스 로그인이 활성화되었습니다." : "페이스 로그인이 비활성화되었습니다.";
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "enabled", enabled
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "페이스 로그인 상태 변경에 실패했습니다."
                ));
            }
            
        } catch (Exception e) {
            log.error("페이스 로그인 상태 변경 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "페이스 로그인 상태 변경 중 오류가 발생했습니다."
            ));
        }
    }
}