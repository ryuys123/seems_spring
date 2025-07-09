package com.test.seems.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * BCrypt 비밀번호 해싱 테스트용 유틸리티
 * 애플리케이션 시작 시 실행되어 BCrypt 해싱을 테스트합니다.
 */
@Component
public class PasswordTest implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== BCrypt 비밀번호 해싱 테스트 ===");
        
        // 테스트용 비밀번호들
        String[] testPasswords = {"test123", "admin123", "password123", "user123"};
        
        for (String password : testPasswords) {
            String hashedPassword = PasswordUtils.encodePassword(password);
            boolean matches = PasswordUtils.matches(password, hashedPassword);
            
            System.out.println("평문 비밀번호: " + password);
            System.out.println("BCrypt 해싱: " + hashedPassword);
            System.out.println("검증 결과: " + (matches ? "성공" : "실패"));
            System.out.println("BCrypt 형태 확인: " + (PasswordUtils.isBCryptHash(hashedPassword) ? "맞음" : "아님"));
            System.out.println("---");
        }
        
        // 잘못된 비밀번호로 검증 테스트
        String wrongPassword = "wrong123";
        String correctHashedPassword = PasswordUtils.encodePassword("test123");
        boolean wrongMatch = PasswordUtils.matches(wrongPassword, correctHashedPassword);
        System.out.println("잘못된 비밀번호 검증: " + (wrongMatch ? "성공" : "실패") + " (예상: 실패)");
        
        System.out.println("=== 테스트 완료 ===");
    }
} 