package com.test.seems.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 해싱 및 검증을 위한 유틸리티 클래스
 * BCrypt 알고리즘을 사용하여 안전한 비밀번호 해싱 제공
 */
@Component
public class PasswordUtils {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 평문 비밀번호를 BCrypt로 해싱
     * @param rawPassword 평문 비밀번호
     * @return 해싱된 비밀번호
     */
    public static String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 평문 비밀번호와 해싱된 비밀번호를 비교
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 해싱된 비밀번호
     * @return 일치 여부
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 비밀번호가 BCrypt 해싱 형태인지 확인
     * @param password 확인할 비밀번호
     * @return BCrypt 해싱 형태 여부
     */
    public static boolean isBCryptHash(String password) {
        return password != null && password.startsWith("$2a$");
    }
    
    /**
     * 비밀번호 강도 검증
     * @param password 검증할 비밀번호
     * @return 유효성 여부
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // 추가적인 비밀번호 정책 검증 로직 추가 가능
        return true;
    }
} 