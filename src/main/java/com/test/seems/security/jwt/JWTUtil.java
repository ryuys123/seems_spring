package com.test.seems.security.jwt;

import com.test.seems.user.model.dto.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 기본 토큰 만들기(생성)  공통
 * 토큰에서 사용자정보(클레임) 추출
 * 클레임에서 userid(subject) 추출
 * 토큰 만료여부 확인용
 * 토큰에서 role (권한) 확인용
 * 토큰 만들 때 토큰 안에 role 지정
 * */
@Slf4j
@Component
public class JWTUtil {

    // application.properties 에 등록한 토큰 관련 값 가져오기
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access_expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh_expiration}")
    private Long refreshExpiration;

    // 토큰 생성 메소드 (공통 : accessToken, refreshToken 둘 다 사용)
    public String generateToken(User user, String category) {
        // 매개변수 선택 : String userId 를 받은 경우, 토큰 안에 Payload 안에 추가할 회원정보가 더 있다면 db 조회 필요함
        //   => db 조회를 하지 않으려면 dto 를 받으면 됨
        // String userId 로 매개변수 지정한다면, JWTUtil 에 memberRepository 의존성 추가하고 findByUserId(userId) : MemberEntity 사용함

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("category", category)  // access | refresh
                .claim("name", user.getUserName())
                .claim("role", user.getAdminYn().equals("Y") ? "ADMIN" : "USER")
                .setExpiration(new Date(
                        System.currentTimeMillis() + (category.equals("access") ? accessExpiration : refreshExpiration)))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // 토큰에서 클레임 추출 (로그인하고 나서 요청하는 서비스일 때 필요함)
    public Claims getClaimsFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("token is empty");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.trim())
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("token expired");
            return e.getClaims();  // 만료된 Claims 반환
        } catch (Exception e) {
            log.error("token error");
            throw e;
        }
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        // JWT 파싱 및 Claims 추출해서 만료 여부 확인
        boolean isExpired = getClaimsFromToken(token).getExpiration().before(new Date());
        // getExpiration() 이 현재 시간보다 이전(지났음)이면 true, 아니면 false
        return isExpired;
    }

    // 토큰에서 사용자 ID 추출
    public String getUseridFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // 토큰에서 role 정보 추출
    public String getAuthorityFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // 토큰에서 카테고리 정보 추출
    public String getCategoryFromToken(String token) {
        return getClaimsFromToken(token).get("category", String.class);
    }

    // 토큰에서 authType 정보 추출
    public String getAuthTypeFromToken(String token) {
        return getClaimsFromToken(token).get("authType", String.class);
    }


    public Long getAccessExpiration() { return accessExpiration; }
    public Long getRefreshExpiration() { return refreshExpiration; }



    // 페이스 로그인 토큰 발급
    public String generateFaceAuthToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("category", "face_auth")
                .claim("authType", "FACE")
                .claim("name", user.getUserName())
                .claim("role", user.getAdminYn().equals("Y") ? "ADMIN" : "USER")
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // 페이스인증 토큰 검증 메소드 추가
    public boolean isFaceAuthToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String category = claims.get("category", String.class);
            String authType = claims.get("authType", String.class);
            return "face_auth".equals(category) && "FACE".equals(authType);
        } catch (Exception e) {
            return false;
        }
    }
    
    // 페이스 로그인 전용 토큰 생성
    public String createFaceJwt(String category, String userId, String userName, String role, Long expiration) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("category", category)
                .claim("authType", "FACE")
                .claim("name", userName)
                .claim("role", role) // 반드시 포함
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
    
    // 토큰에서 카테고리 추출
    public String getCategory(String token) {
        return getClaimsFromToken(token).get("category", String.class);
    }
    
    // 토큰에서 사용자 ID 추출 (String 타입)
    public String getUserId(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return getClaimsFromToken(token).get("name", String.class);
    }



}

















