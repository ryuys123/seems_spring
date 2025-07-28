package com.test.seems.social.controller;

import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.social.jpa.entity.SocialLoginEntity;
import com.test.seems.social.jpa.repository.SocialLoginRepository;
import com.test.seems.social.model.dto.SocialLoginDto;
import com.test.seems.social.model.dto.SocialSignupCompleteDto;
import com.test.seems.social.model.service.SocialLoginService;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialLoginService socialLoginService;
    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 세션 스토어 (실제 운영에서는 Redis 사용 권장)
    private final Map<String, SocialSessionData> sessionStore = new ConcurrentHashMap<>();

    // 세션 데이터 클래스
    @Data
    @Builder
    public static class SocialSessionData {
        private String provider;
        private String socialId;
        private String socialEmail;
        private String userName;
        private String email;
        private long createdAt;
        private static final long EXPIRE_TIME = 10 * 60 * 1000; // 10분
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > EXPIRE_TIME;
        }
    }

    // OAuth2 인증 요청 처리
    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<?> oauth2Authorization(@PathVariable String provider) {
        // 각 소셜 로그인 제공자의 인증 URL로 리다이렉트
        String authUrl = "";
        switch (provider.toLowerCase()) {
            case "google":
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=879071102220-nueg754t7m080rhmufebf5rejv29n3lt.apps.googleusercontent.com" +
                        "&redirect_uri=http://localhost:8888/seems/auth/social/callback" +
                        "&response_type=code" +
                        "&scope=openid%20email%20profile" +
                        "&state=" + provider;
                break;
            case "kakao":
                authUrl = "https://kauth.kakao.com/oauth/authorize?" +
                        "client_id=78b84e63c94b938a6a1e31afd09c4522" +
                        "&redirect_uri=http://localhost:8888/seems/auth/social/callback" +
                        "&response_type=code" +
                        "&state=" + provider;
                break;
            case "naver":
                authUrl = "https://nid.naver.com/oauth2.0/authorize?" +
                        "client_id=uKWQhJvKZ2Po7xv32GkC" +
                        "&redirect_uri=http://localhost:8888/seems/auth/social/callback" +
                        "&response_type=code" +
                        "&state=" + provider;
                break;
            default:
                return ResponseEntity.badRequest().body("지원하지 않는 소셜 로그인입니다.");
        }
        
        return ResponseEntity.ok().body("{\"authUrl\":\"" + authUrl + "\"}");
    }

    // 소셜 로그인 콜백 처리 (OAuth2 리다이렉트 처리)
    @GetMapping("/social/callback")
    public void socialCallback(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletResponse response) throws IOException {
        
        log.info("소셜 로그인 콜백 - code: {}, state: {}, error: {}", code, state, error);
        
        if (error != null) {
            String errorMsg = errorDescription != null ? errorDescription : error;
            sendErrorResponse(response, "소셜 로그인에 실패했습니다: " + errorMsg);
            return;
        }
        
        if (code == null || code.isEmpty()) {
            sendErrorResponse(response, "인증 코드가 없습니다.");
            return;
        }
        
        try {
            // provider 결정 로직 개선
            String provider;
            if (state != null && !state.isEmpty()) {
                provider = state.toLowerCase();
                log.info("State에서 provider 추출: {}", provider);
            } else {
                // state가 없으면 에러 처리 (기본값 대신)
                log.error("State 파라미터가 누락되었습니다. code: {}", code);
                sendErrorResponse(response, "소셜 로그인 상태 정보가 누락되었습니다.");
                return;
            }
            
            log.info("소셜 로그인 처리 시작: provider={}, code={}", provider, code);
            Map<String, String> userInfo = null;
            
            // 각 소셜 로그인별로 사용자 정보 가져오기
            switch (provider) {
                case "google":
                    log.info("Google 사용자 정보 조회 시작");
                    userInfo = socialLoginService.getGoogleUserInfo(code);
                    break;
                case "naver":
                    log.info("Naver 사용자 정보 조회 시작");
                    userInfo = socialLoginService.getNaverUserInfo(code);
                    break;
                case "kakao":
                    log.info("Kakao 사용자 정보 조회 시작");
                    userInfo = socialLoginService.getKakaoUserInfo(code);
                    break;
                default:
                    log.error("지원하지 않는 소셜 로그인: {}", provider);
                    sendErrorResponse(response, "지원하지 않는 소셜 로그인입니다: " + provider);
                    return;
            }
            
            if (userInfo == null) {
                log.error("{} 사용자 정보 조회 실패", provider);
                sendErrorResponse(response, provider + " 사용자 정보를 가져올 수 없습니다.");
                return;
            }
            
            log.info("{} 사용자 정보 조회 성공: {}", provider, userInfo);
            
            String userEmail = userInfo.get("email");
            String userName = userInfo.get("name");
            String socialId = userInfo.get("id");
            String socialEmail = userInfo.get("email"); // 소셜 계정의 이메일

            // 소셜 로그인 정보로 사용자 조회 (provider + social_id(고유 id) 기준)
            Optional<SocialLoginEntity> existingSocialLogin = socialLoginRepository.findByProviderAndSocialId(provider, socialId);
            UserEntity userEntity;

            if (existingSocialLogin.isPresent()) {
                // 기존 소셜 사용자 - 바로 로그인
                userEntity = existingSocialLogin.get().getUser();
                log.info("기존 소셜 사용자 로그인: {} ({})", userEntity.getUserId(), provider);
                // JWT 토큰 생성
                String token = jwtUtil.generateToken(userEntity.toDto(), "access");
                log.info("JWT 토큰 생성 완료: {}", token != null ? "토큰 생성됨" : "토큰 생성 실패");
                log.info("생성된 토큰 길이: {}, 토큰 시작: {}", 
                    token != null ? token.length() : 0, 
                    token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
                if (token == null || token.isEmpty()) {
                    log.error("JWT 토큰이 null이거나 빈 문자열입니다.");
                    sendErrorResponse(response, "토큰 생성에 실패했습니다.");
                    return;
                }
                // 기존 사용자는 바로 성공 페이지 반환 (대시보드로 이동)
                // refreshToken도 함께 생성
                String refreshToken = jwtUtil.generateToken(userEntity.toDto(), "refresh");
                log.info("RefreshToken 생성 완료: {}", refreshToken != null ? "토큰 생성됨" : "토큰 생성 실패");
                log.info("프론트엔드로 전송할 토큰 정보 - userId: {}, userName: {}, email: {}", 
                    userEntity.getUserId(), userEntity.getUserName(), userEntity.getEmail());
                sendJsonResponse(response, createLoginSuccessResponse(token, refreshToken, userEntity.getUserName(), userEntity.getUserId(), userEntity.getEmail()));
            } else {
                // 신규 소셜 사용자 - 세션 ID 방식으로 처리
                log.info("신규 소셜 사용자: {} ({})", socialId, provider);
                
                // 세션 ID 생성 및 데이터 저장
                String sessionId = UUID.randomUUID().toString();
                SocialSessionData sessionData = SocialSessionData.builder()
                        .provider(provider)
                        .socialId(socialId)
                        .socialEmail(socialEmail)
                        .userName(userName)
                        .email(userEmail)
                        .createdAt(System.currentTimeMillis())
                        .build();
                
                sessionStore.put(sessionId, sessionData);
                log.info("신규 사용자 세션 생성: sessionId={}, provider={}", sessionId, provider);
                
                // 만료된 세션 정리
                cleanExpiredSessions();
                
                // 신규 사용자 - 추가 정보 입력 필요 JSON 응답 (세션 ID 사용)
                sendJsonResponse(response, createAdditionalInfoNeededResponse(sessionId, userEmail, userName, provider, socialId, socialEmail));
            }
            
        } catch (Exception e) {
            log.error("소셜 로그인 콜백 처리 중 오류: ", e);
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 소셜 로그인 콜백에서 소셜 정보로 회원 찾기
    @PostMapping("/social/login")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginDto dto) {
        return socialLoginService.findUserBySocial(dto.getProvider(), dto.getSocialId())
                .map(user -> {
                    // 토큰 발급 등 로그인 처리
                    return ResponseEntity.ok().body("로그인 성공");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("회원가입 필요"));
    }

    // 소셜 회원가입 완료 (추가 정보 저장) - HTML + postMessage 방식
    @PostMapping("/social/complete-signup")
    public void completeSocialSignup(@RequestBody SocialSignupCompleteDto dto, HttpServletResponse response) throws IOException {
        try {
            log.info("소셜 회원가입 완료 요청: sessionId={}, userId={}, provider={}", 
                dto.getSessionId(), dto.getUserId(), dto.getProvider());
            
            // 1. 세션 ID 검증 및 데이터 복원
            SocialSessionData sessionData = null;
            if (dto.getSessionId() != null) {
                sessionData = sessionStore.get(dto.getSessionId());
                if (sessionData == null || sessionData.isExpired()) {
                    sendErrorResponse(response, "세션이 만료되었거나 존재하지 않습니다. 다시 소셜 로그인을 시도해주세요.");
                    return;
                }
                log.info("세션 데이터 복원 성공: sessionId={}, provider={}", dto.getSessionId(), sessionData.getProvider());
            } else {
                sendErrorResponse(response, "세션 ID가 필요합니다.");
                return;
            }
            
            // 2. 세션 데이터로 누락된 정보 보완
            if (dto.getProvider() == null || dto.getSocialId() == null) {
                dto.setProvider(sessionData.getProvider());
                dto.setSocialId(sessionData.getSocialId());
                dto.setSocialEmail(sessionData.getSocialEmail());
                log.info("세션 데이터로 DTO 보완: provider={}, socialId={}", sessionData.getProvider(), sessionData.getSocialId());
            }
            
            // 3. 비밀번호 검증 (자체 회원가입과 동일한 조건)
            if (dto.getUserPwd() == null || dto.getUserPwd().length() < 8) {
                sendErrorResponse(response, "비밀번호는 8자 이상이어야 합니다.");
                return;
            }
            
            // 4. 비밀번호 복잡성 검증 (영문+숫자+특수문자)
            if (!dto.getUserPwd().matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$")) {
                sendErrorResponse(response, "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.");
                return;
            }
            
            // 5. 프로필 이미지 처리 (base64 → 파일 저장 방식으로 변경)
            String profileImagePath = null;
            if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
                try {
                    profileImagePath = saveBase64ImageAsFile(dto.getProfileImage(), dto.getUserId());
                    log.info("프로필 이미지 파일 저장 완료: {}", profileImagePath);
                } catch (Exception e) {
                    log.error("프로필 이미지 저장 실패: {}", e.getMessage());
                    sendErrorResponse(response, "프로필 이미지 저장에 실패했습니다: " + e.getMessage());
                    return;
                }
            }
            
            // 6. 사용자 정보 생성 및 저장
            UserEntity userEntity = UserEntity.builder()
                    .userId(dto.getUserId())
                    .userName(dto.getUserName())
                    .email(null) // email은 사용하지 않음
                    .phone(dto.getPhone())
                    .userPwd(passwordEncoder.encode(dto.getUserPwd())) // 비밀번호 암호화
                    .profileImage(profileImagePath)
                    .status(1) // 활성 상태
                    .adminYn("N")
                    .faceLoginEnabled(false)
                    .createdAt(new java.util.Date())
                    .updatedAt(new java.util.Date())
                    .build();
            
            userRepository.save(userEntity);
            log.info("사용자 정보 저장 완료: {}", userEntity.getUserId());
            
            // 7. 소셜 로그인 정보 저장 (tb_user_social_login 테이블)
            try {
                socialLoginService.registerSocialUser(userEntity, dto.getProvider(), dto.getSocialId(), dto.getSocialEmail());
                log.info("소셜 로그인 정보 저장 완료: {} ({}) - social_id: {}, social_email: {}", 
                    userEntity.getUserId(), dto.getProvider(), dto.getSocialId(), dto.getSocialEmail());
            } catch (Exception e) {
                log.warn("소셜 로그인 정보 저장 실패: {}", e.getMessage());
                sendErrorResponse(response, "소셜 로그인 정보 저장에 실패했습니다.");
                return;
            }
            
            // 8. JWT 토큰 생성
            String token = jwtUtil.generateToken(userEntity.toDto(), "access");
            String refreshToken = jwtUtil.generateToken(userEntity.toDto(), "refresh");
            
            if (token == null || refreshToken == null) {
                sendErrorResponse(response, "토큰 생성에 실패했습니다.");
                return;
            }
            
            // 9. 성공 응답 (HTML + postMessage 방식)
            log.info("소셜 회원가입 완료: userId={}, provider={}", userEntity.getUserId(), dto.getProvider());
            
            // 10. 사용 완료된 세션 데이터 정리
            if (dto.getSessionId() != null) {
                sessionStore.remove(dto.getSessionId());
                log.info("회원가입 완료 후 세션 정리: sessionId={}", dto.getSessionId());
            }
            
            // HTML + postMessage로 토큰 전달
            sendJsonResponse(response, createLoginSuccessResponse(token, refreshToken, userEntity.getUserName(), userEntity.getUserId(), userEntity.getEmail()));
            
        } catch (Exception e) {
            log.error("소셜 회원가입 완료 처리 중 오류: ", e);
            sendErrorResponse(response, "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // ========== JSON 응답 메서드들 ==========
    
    /**
     * HTML 페이지로 postMessage 전송 (프론트엔드 모달 연동)
     */
    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        // JSON 데이터를 JavaScript 객체로 변환
        StringBuilder jsObject = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) jsObject.append(",");
            jsObject.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                jsObject.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Boolean) {
                jsObject.append(entry.getValue().toString());
            } else {
                jsObject.append("\"").append(entry.getValue()).append("\"");
            }
            first = false;
        }
        jsObject.append("}");
        
        // HTML 페이지로 postMessage 전송
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>소셜 로그인 처리 중</title>
                <meta charset="UTF-8">
                <script>
                    window.onload = function() {
                        const data = %s;
                        
                        console.log('소셜 로그인 처리 완료, postMessage 전송:', data);
                        
                        if (window.opener) {
                            window.opener.postMessage(data, "*");
                            console.log('postMessage 전송 완료');
                        } else {
                            console.error('window.opener가 없습니다.');
                        }
                        
                        // 잠시 후 창 닫기
                        setTimeout(() => { window.close(); }, 1000);
                    };
                </script>
            </head>
            <body>
                <div style="text-align: center; padding: 50px; font-family: Arial, sans-serif;">
                    <h2>🔄 처리 중...</h2>
                    <p>잠시만 기다려주세요.</p>
                </div>
            </body>
            </html>
            """, jsObject.toString());
        
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
    
    /**
     * 신규 사용자 - 추가 정보 입력 필요 응답 생성 (세션 ID 방식)
     */
    private Map<String, Object> createAdditionalInfoNeededResponse(String sessionId, String email, String userName, String provider, String socialId, String socialEmail) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("type", "social-signup-needed"); // 프론트엔드 type 필드 추가
        responseData.put("status", "additional_info_needed");
        responseData.put("message", "추가 정보 입력이 필요합니다");
        responseData.put("sessionId", sessionId); // tempToken → sessionId 변경
        responseData.put("provider", provider);
        responseData.put("socialId", socialId);
        responseData.put("socialEmail", socialEmail != null ? socialEmail : "");
        responseData.put("userName", userName != null ? userName : "");
        responseData.put("email", email != null ? email : "");
        responseData.put("isExistingUser", false);
        
        log.info("신규 사용자 추가 정보 필요 응답 생성: provider={}, userName={}, sessionId={}", provider, userName, sessionId);
        return responseData;
    }
    
    /**
     * 만료된 세션 정리
     */
    private void cleanExpiredSessions() {
        sessionStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("만료된 세션 정리 완료. 현재 세션 수: {}", sessionStore.size());
    }
    
    /**
     * 기존 사용자 - 로그인 성공 응답 생성
     */
    private Map<String, Object> createLoginSuccessResponse(String token, String refreshToken, String userName, String userId, String email) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("type", "social-login-success"); // 프론트엔드 type 필드 추가
        responseData.put("status", "login_success");
        responseData.put("message", "로그인이 완료되었습니다");
        responseData.put("token", token);
        responseData.put("refreshToken", refreshToken != null ? refreshToken : "");
        responseData.put("userName", userName != null ? userName : "");
        responseData.put("userId", userId != null ? userId : "");
        responseData.put("email", email != null ? email : "");
        responseData.put("isExistingUser", true);
        
        log.info("기존 사용자 로그인 성공 응답 생성: userId={}, userName={}", userId, userName);
        return responseData;
    }
    
    /**
     * 오류 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", errorMessage);
        
        sendJsonResponse(response, errorResponse);
        log.info("소셜 로그인 오류 페이지 반환 완료");
    }

    /**
     * base64 이미지 데이터를 실제 파일로 저장
     * @param base64Image base64 인코딩된 이미지 데이터
     * @param userId 사용자 ID (파일명에 사용)
     * @return 저장된 파일명
     */
    private String saveBase64ImageAsFile(String base64Image, String userId) throws IOException {
        // base64 데이터에서 헤더 제거 (data:image/jpeg;base64, 부분)
        String base64Data;
        String fileExtension = "jpg"; // 기본값
        
        if (base64Image.startsWith("data:")) {
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("잘못된 base64 이미지 형식입니다.");
            }
            
            // MIME 타입에서 확장자 추출
            String mimeType = parts[0];
            if (mimeType.contains("image/png")) {
                fileExtension = "png";
            } else if (mimeType.contains("image/gif")) {
                fileExtension = "gif";
            } else if (mimeType.contains("image/jpeg") || mimeType.contains("image/jpg")) {
                fileExtension = "jpg";
            }
            
            base64Data = parts[1];
        } else {
            // 헤더가 없는 순수 base64 데이터
            base64Data = base64Image;
        }

        // base64 디코딩
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("base64 디코딩에 실패했습니다: " + e.getMessage());
        }

        // 저장 디렉토리 생성
        String savePath = uploadDir + "/photo";
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일명 생성 (자체로그인과 동일한 방식)
        String fileName = userId + "_profile_social." + fileExtension;
        File imageFile = new File(savePath, fileName);

        // 파일 크기 체크 (10MB 제한)
        if (imageBytes.length > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 파일이 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        // 파일 저장
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(imageBytes);
        }

        log.info("소셜 프로필 이미지 저장 완료: {} (크기: {} bytes)", fileName, imageBytes.length);
        return fileName;
    }
}