package com.test.seems.security.handler;

import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.user.jpa.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공 핸들러 실행");

        try {
            // 간단한 성공 응답
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
                                        message: '소셜 로그인 성공'
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
            """;
            
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(html);
            response.getWriter().flush();
            log.info("OAuth2 로그인 성공 페이지 반환 완료");
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류: ", e);
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }
    
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
        log.info("OAuth2 로그인 오류 페이지 반환 완료");
    }
} 