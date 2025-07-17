package com.test.seems.face.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceSignupResponse {
    
    /**
     * 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 사용자 ID
     */
    private String userId;
    
    /**
     * 사용자 이름
     */
    private String username;
    
    /**
     * 페이스 로그인 ID
     */
    private Long faceLoginId;
    
    /**
     * 성공 응답 생성
     */
    public static FaceSignupResponse success(String userId, String username, Long faceLoginId) {
        return FaceSignupResponse.builder()
                .success(true)
                .message("페이스 회원가입이 완료되었습니다.")
                .userId(userId)
                .username(username)
                .faceLoginId(faceLoginId)
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static FaceSignupResponse failure(String message) {
        return FaceSignupResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 