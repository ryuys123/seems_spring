package com.test.seems.social.model.service;

import com.test.seems.social.jpa.entity.SocialLoginEntity;
import com.test.seems.social.jpa.repository.SocialLoginRepository;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {
    private final SocialLoginRepository socialLoginRepository;
    private final UserRepository userRepository;

    // OAuth2 설정
    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    
    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.naver.client-id}")
    private String naverClientId;
    
    @Value("${oauth2.naver.client-secret}")
    private String naverClientSecret;
    
    @Value("${oauth2.naver.redirect-uri}")
    private String naverRedirectUri;

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${oauth2.kakao.client-secret}")
    private String kakaoClientSecret;
    
    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // 소셜 로그인 정보로 UserEntity 찾기
    public Optional<UserEntity> findUserBySocial(String provider, String socialId) {
        return socialLoginRepository.findByProviderAndSocialId(provider, socialId)
                .map(SocialLoginEntity::getUser);
    }

    // 소셜 회원가입 (UserEntity 생성 후 SocialLoginEntity 생성)
    public SocialLoginEntity registerSocialUser(UserEntity user, String provider, String socialId, String socialEmail) {
        SocialLoginEntity entity = SocialLoginEntity.builder()
                .user(user)
                .provider(provider)
                .socialId(socialId)
                .socialEmail(socialEmail)
                .linkedAt(new Date())
                .build();
        return socialLoginRepository.save(entity);
    }

    // Google OAuth2 사용자 정보 가져오기
    public Map<String, String> getGoogleUserInfo(String code) {
        try {
            // 1. 인증 코드로 액세스 토큰 요청 (일반 OAuth2 방식)
            String tokenUrl = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String tokenBody = String.format(
                "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
                googleClientId, googleClientSecret, code, googleRedirectUri
            );
            
            log.info("Google 토큰 요청 - client_id: {}, redirect_uri: {}", googleClientId, googleRedirectUri);
            log.info("토큰 요청 URL: {}", tokenUrl);
            
            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                log.error("Google 토큰 요청 실패: {}", tokenResponse.getStatusCode());
                log.error("응답 본문: {}", tokenResponse.getBody());
                return null;
            }
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            // 2. 액세스 토큰으로 사용자 정보 요청
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class
            );
            
            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                log.error("Google 사용자 정보 요청 실패: {}", userInfoResponse.getStatusCode());
                return null;
            }
            
            Map<String, Object> userInfo = userInfoResponse.getBody();
            Map<String, String> result = new HashMap<>();
            result.put("id", (String) userInfo.get("id")); // Google의 고유 id
            result.put("email", (String) userInfo.get("email"));
            result.put("name", (String) userInfo.get("name"));
            
            log.info("Google 사용자 정보 조회 성공: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Google 사용자 정보 조회 실패", e);
            return null;
        }
    }

    // Naver OAuth2 사용자 정보 가져오기
    public Map<String, String> getNaverUserInfo(String code) {
        try {
            // 1. 인증 코드로 액세스 토큰 요청
            String tokenUrl = "https://nid.naver.com/oauth2.0/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String tokenBody = String.format(
                "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&state=naver",
                naverClientId, naverClientSecret, code
            );
            
            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                log.error("Naver 토큰 요청 실패: {}", tokenResponse.getStatusCode());
                return null;
            }
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            // 2. 액세스 토큰으로 사용자 정보 요청
            String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class
            );
            
            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                log.error("Naver 사용자 정보 요청 실패: {}", userInfoResponse.getStatusCode());
                return null;
            }
            
            Map<String, Object> response = userInfoResponse.getBody();
            Map<String, Object> userInfo = (Map<String, Object>) response.get("response");
            
            Map<String, String> result = new HashMap<>();
            result.put("id", (String) userInfo.get("id")); // Naver의 고유 id
            result.put("email", (String) userInfo.get("email"));
            result.put("name", (String) userInfo.get("name"));
            
            log.info("Naver 사용자 정보 조회 성공: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Naver 사용자 정보 조회 실패", e);
            return null;
        }
    }

    // Kakao OAuth2 사용자 정보 가져오기
    public Map<String, String> getKakaoUserInfo(String code) {
        try {
            // 1. 인증 코드로 액세스 토큰 요청
            String tokenUrl = "https://kauth.kakao.com/oauth/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String tokenBody = String.format(
                "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
                kakaoClientId, kakaoClientSecret, code, kakaoRedirectUri
            );
            
            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
                log.error("Kakao 토큰 요청 실패: {}", tokenResponse.getStatusCode());
                return null;
            }
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            // 2. 액세스 토큰으로 사용자 정보 요청
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class
            );
            
            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                log.error("Kakao 사용자 정보 요청 실패: {}", userInfoResponse.getStatusCode());
                return null;
            }
            
            Map<String, Object> userInfo = userInfoResponse.getBody();
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            Map<String, String> result = new HashMap<>();
            String kakaoId = userInfo.get("id").toString(); // 블록 밖에서 선언
            String email = (String) kakaoAccount.get("email");
            if (email != null && !email.isEmpty()) {
                result.put("email", email);
            } else {
                result.put("email", "kakao_" + kakaoId + "@kakao.com");
            }
            result.put("name", (String) profile.get("nickname"));
            result.put("id", kakaoId); // 반드시 숫자 id만 사용
            
            log.info("Kak ao 사용자 정보 조회 성공: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 실패", e);
            return null;
        }
    }
} 