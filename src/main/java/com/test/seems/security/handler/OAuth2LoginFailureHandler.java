package com.test.seems.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        log.error("OAuth2 로그인 실패: {}", exception.getMessage());
        
        // 간단한 실패 페이지 반환
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>소셜 로그인 실패</title>
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
                                    type: "social-login-failure",
                                    error: "소셜 로그인에 실패했습니다."
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
                    <h2>❌ 소셜 로그인 실패</h2>
                    <p>인증에 실패했습니다.</p>
                </div>
            </body>
            </html>
        """);
        response.getWriter().flush();
        
        log.info("OAuth2 로그인 실패 페이지 반환 완료");
    }
} 