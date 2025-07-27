package com.test.seems.social.controller;

import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.social.jpa.entity.SocialLoginEntity;
import com.test.seems.social.jpa.repository.SocialLoginRepository;
import com.test.seems.social.model.dto.SocialLoginDto;
import com.test.seems.social.model.dto.SocialSignupCompleteDto;
import com.test.seems.social.model.service.SocialLoginService;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    // OAuth2 인증 요청 처리
    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<?> oauth2Authorization(@PathVariable String provider) {
        // 각 소셜 로그인 제공자의 인증 URL로 리다이렉트
        String authUrl = "";
        switch (provider.toLowerCase()) {
            case "google":
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=88204759456-9e7upkfu68je4ub0r2kqa0q93ih4684b.apps.googleusercontent.com" +
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
                        "client_id=0ZekT2rsq3OALah3xByD" +
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
            String provider = state != null ? state : "google";
            Map<String, String> userInfo = null;
            
            // 각 소셜 로그인별로 사용자 정보 가져오기
            switch (provider.toLowerCase()) {
                case "google":
                    userInfo = socialLoginService.getGoogleUserInfo(code);
                    break;
                case "naver":
                    userInfo = socialLoginService.getNaverUserInfo(code);
                    break;
                case "kakao":
                    userInfo = socialLoginService.getKakaoUserInfo(code);
                    break;
                default:
                    sendErrorResponse(response, "지원하지 않는 소셜 로그인입니다.");
                    return;
            }
            
            if (userInfo == null) {
                sendErrorResponse(response, "사용자 정보를 가져올 수 없습니다.");
                return;
            }
            
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
                if (token == null || token.isEmpty()) {
                    log.error("JWT 토큰이 null이거나 빈 문자열입니다.");
                    sendErrorResponse(response, "토큰 생성에 실패했습니다.");
                    return;
                }
                // 기존 사용자는 바로 성공 페이지 반환 (대시보드로 이동)
                // refreshToken도 함께 생성
                String refreshToken = jwtUtil.generateToken(userEntity.toDto(), "refresh");
                sendSuccessResponse(response, token, refreshToken, userEntity.getUserName(), userEntity.getUserId(), userEntity.getEmail());
            } else {
                // 신규 소셜 사용자 - 추가 정보 입력 페이지로 이동
                log.info("신규 소셜 사용자: {} ({})", socialId, provider);
                // 임시 사용자 정보로 토큰 생성 (추가 정보 입력용)
                UserEntity tempUser = UserEntity.builder()
                        .userId("temp_" + System.currentTimeMillis()) // 임시 ID
                        .userName(userName != null ? userName : "소셜사용자")
                        .email(null) // email은 사용하지 않음
                        .phone("")
                        .userPwd("")
                        .status(0) // 임시 상태 (추가 정보 입력 전)
                        .adminYn("N")
                        .faceLoginEnabled(false)
                        .build();
                // 임시 토큰 생성 (추가 정보 입력 페이지로 이동)
                String tempToken = jwtUtil.generateToken(tempUser.toDto(), "access");
                log.info("신규 사용자 임시 토큰 생성: {}", tempToken != null ? "토큰 생성됨" : "토큰 생성 실패");
                if (tempToken == null || tempToken.isEmpty()) {
                    log.error("임시 JWT 토큰이 null이거나 빈 문자열입니다.");
                    sendErrorResponse(response, "토큰 생성에 실패했습니다.");
                    return;
                }
                // 추가 정보 입력 페이지로 리다이렉트 (social_id에 고유 id 저장)
                sendAdditionalInfoResponse(response, tempToken, userEmail, userName, provider, socialId, socialEmail);
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

    // 소셜 회원가입 완료 (추가 정보 저장)
    @PostMapping("/social/complete-signup")
    public ResponseEntity<?> completeSocialSignup(@RequestBody SocialSignupCompleteDto dto) {
        try {
            log.info("소셜 회원가입 완료 요청: {}", dto.getEmail());
            
            // 1. 사용자 정보 업데이트 (email은 사용하지 않음)
            UserEntity userEntity = UserEntity.builder()
                    .userId(dto.getUserId())
                    .userName(dto.getUserName())
                    .email(null) // email은 사용하지 않음
                    .phone(dto.getPhone())
                    .userPwd(passwordEncoder.encode(dto.getUserPwd())) // 비밀번호 암호화
                    .status(1) // 활성 상태로 변경
                    .adminYn("N")
                    .faceLoginEnabled(false)
                    .build();
            
            userRepository.save(userEntity);
            log.info("사용자 정보 저장 완료: {}", userEntity.getUserId());
            
            // 2. 소셜 로그인 정보 저장 (tb_user_social_login 테이블, social_id에 고유 id 저장)
            try {
                socialLoginService.registerSocialUser(userEntity, dto.getProvider(), dto.getSocialId(), dto.getSocialEmail());
                log.info("소셜 로그인 정보 저장 완료: {} ({}) - social_id: {}, social_email: {}", userEntity.getUserId(), dto.getProvider(), dto.getSocialId(), dto.getSocialEmail());
            } catch (Exception e) {
                log.warn("소셜 로그인 정보 저장 실패: {}", e.getMessage());
            }
            
            // 3. 새로운 JWT 토큰 생성
            String token = jwtUtil.generateToken(userEntity.toDto(), "access");
            String refreshToken = jwtUtil.generateToken(userEntity.toDto(), "refresh");
            
            // 4. 성공 응답
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("userName", userEntity.getUserName());
            response.put("userId", userEntity.getUserId());
            response.put("email", userEntity.getEmail());
            response.put("isExistingUser", false);
            response.put("message", "회원가입이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("소셜 회원가입 완료 처리 중 오류: ", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 추가 정보 입력 응답 페이지
    private void sendAdditionalInfoResponse(HttpServletResponse response, String tempToken, String email, String userName, String provider, String socialId, String socialEmail) throws IOException {
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>추가 정보 입력</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f5f5f5; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .form-group { margin-bottom: 20px; text-align: left; }
                    label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
                    input { width: 100%%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 16px; }
                    button { background: #007bff; color: white; padding: 12px 30px; border: none; border-radius: 5px; font-size: 16px; cursor: pointer; }
                    button:hover { background: #0056b3; }
                    .info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                </style>
                <script>
                    function submitAdditionalInfo() {
                        const userId = document.getElementById('userId').value;
                        const userPwd = document.getElementById('userPwd').value;
                        const userName = document.getElementById('userName').value;
                        const phone = document.getElementById('phone').value;
                        
                        if (!userId || !userPwd || !userName || !phone) {
                            alert('모든 필드를 입력해주세요.');
                            return;
                        }
                        
                        // 추가 정보 저장 API 호출
                        fetch('/seems/auth/social/complete-signup', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Authorization': 'Bearer ' + '%s'
                            },
                            body: JSON.stringify({
                                userId: userId,
                                userPwd: userPwd,
                                userName: userName,
                                phone: phone,
                                email: '%s',
                                provider: '%s',
                                socialId: '%s',
                                socialEmail: '%s'
                            })
                        })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                // 성공 시 부모 창에 메시지 전송
                                if (window.opener) {
                                                                    window.opener.postMessage({
                                    type: 'social-signup-complete',
                                    token: data.token,
                                    refreshToken: data.refreshToken,
                                    userName: data.userName,
                                    userId: data.userId,
                                    email: data.email,
                                    isExistingUser: false
                                }, "*");
                                }
                                window.close();
                            } else {
                                alert('회원가입 중 오류가 발생했습니다: ' + data.message);
                            }
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            alert('회원가입 중 오류가 발생했습니다.');
                        });
                    }
                </script>
            </head>
            <body>
                <div class="container">
                    <h2>🎉 소셜 로그인 성공!</h2>
                    <div class="info">
                        <p><strong>%s</strong> 계정으로 로그인되었습니다.</p>
                        <p>서비스 이용을 위해 추가 정보를 입력해주세요.</p>
                    </div>
                    
                    <form onsubmit="event.preventDefault(); submitAdditionalInfo();">
                        <div class="form-group">
                            <label for="userId">아이디 *</label>
                            <input type="text" id="userId" placeholder="사용할 아이디를 입력하세요" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="userPwd">비밀번호 *</label>
                            <input type="password" id="userPwd" placeholder="비밀번호를 입력하세요" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="userName">이름 *</label>
                            <input type="text" id="userName" value="%s" placeholder="이름을 입력하세요" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="phone">전화번호 *</label>
                            <input type="tel" id="phone" placeholder="전화번호를 입력하세요 (예: 010-1234-5678)" required>
                        </div>
                        
                        <button type="submit">회원가입 완료</button>
                    </form>
                </div>
            </body>
            </html>
            """, tempToken, email, provider, socialId, socialEmail, provider.toUpperCase(), userName);
        
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
    
    // 성공 응답 페이지
    private void sendSuccessResponse(HttpServletResponse response, String token, String refreshToken, String userName, String userId, String email) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>소셜 로그인 성공</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    .success { background: #d4edda; padding: 20px; border-radius: 5px; margin: 20px; }
                </style>
                <script>
                    function sendMessageAndClose() {
                        try {
                            if (window.opener) {
                                window.opener.postMessage({ 
                                    type: 'social-login-success',
                                    token: '%s',
                                    refreshToken: '%s',
                                    userName: '%s',
                                    userId: '%s',
                                    email: '%s',
                                    isExistingUser: true
                                }, "*");
                            }
                        } catch (error) {
                            console.error("메시지 전송 중 오류:", error);
                        } finally {
                            setTimeout(() => { window.close(); }, 1000);
                        }
                    }
                    window.onload = function() { sendMessageAndClose(); };
                </script>
            </head>
            <body>
                <div class="success">
                    <h2>✅ 소셜 로그인 성공!</h2>
                    <p>인증이 완료되었습니다.</p>
                </div>
            </body>
            </html>
        """.formatted(token, refreshToken, userName, userId, email);
        
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
        log.info("소셜 로그인 성공 페이지 반환 완료");
    }
    
    // 오류 응답 페이지
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>소셜 로그인 오류</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    .error { background: #f8d7da; padding: 20px; border-radius: 5px; margin: 20px; }
                </style>
                <script>
                    function sendErrorMessageAndClose() {
                        try {
                            if (window.opener) {
                                window.opener.postMessage({ 
                                    type: 'social-login-failure',
                                    error: '%s'
                                }, "*");
                            }
                        } catch (error) {
                            console.error("메시지 전송 중 오류:", error);
                        } finally {
                            setTimeout(() => { window.close(); }, 1000);
                        }
                    }
                    window.onload = function() { sendErrorMessageAndClose(); };
                </script>
            </head>
            <body>
                <div class="error">
                    <h2>❌ 소셜 로그인 오류</h2>
                    <p>%s</p>
                </div>
            </body>
            </html>
        """.formatted(errorMessage, errorMessage);
        
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
        log.info("소셜 로그인 오류 페이지 반환 완료");
    }
}