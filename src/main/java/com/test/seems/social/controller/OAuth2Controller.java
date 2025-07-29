package com.test.seems.social.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
public class OAuth2Controller {

    // Google OAuth2 설정
    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    
    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;
    
    @Value("${oauth2.google.scope}")
    private String googleScope;

    // Naver OAuth2 설정
    @Value("${oauth2.naver.client-id}")
    private String naverClientId;
    
    @Value("${oauth2.naver.redirect-uri}")
    private String naverRedirectUri;

    // Kakao OAuth2 설정
    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    @Value("${oauth2.kakao.scope}")
    private String kakaoScope;

    // React 코드와 호환되는 OAuth2 인증 요청 처리
    @GetMapping("/oauth2/authorization/{provider}")
    public void oauth2Authorization(@PathVariable String provider, HttpServletResponse response) throws IOException {
        log.info("OAuth2 인증 요청: {}", provider);
        
        // 각 소셜 로그인 제공자의 인증 URL로 리다이렉트
        String authUrl = "";
        switch (provider.toLowerCase()) {
            case "google":
                // Google OAuth2 - 일반 방식 (PKCE 제거)
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=" + googleClientId +
                        "&redirect_uri=" + googleRedirectUri +
                        "&response_type=code" +
                        "&scope=" + googleScope +
                        "&state=" + provider;
                break;
            case "kakao":
                authUrl = "https://kauth.kakao.com/oauth/authorize?" +
                        "client_id=" + kakaoClientId +
                        "&redirect_uri=" + kakaoRedirectUri +
                        "&response_type=code" +
                        "&scope=" + kakaoScope +
                        "&state=" + provider;
                break;
            case "naver":
                authUrl = "https://nid.naver.com/oauth2.0/authorize?" +
                        "client_id=" + naverClientId +
                        "&redirect_uri=" + naverRedirectUri +
                        "&response_type=code" +
                        "&state=" + provider;
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("지원하지 않는 소셜 로그인입니다.");
                return;
        }
        
        log.info("OAuth2 인증 URL로 리다이렉트: {}", authUrl);
        response.sendRedirect(authUrl);
    }
} 