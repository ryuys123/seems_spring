package com.test.seems.face.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceLoginResponse {

    /**
     * 로그인 성공 여부
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
    private String userName;
    
    /**
     * 액세스 토큰
     */
    private String accessToken;
    
    /**
     * 리프레시 토큰
     */
    private String refreshToken;
    
    /**
     * 페이스 이름
     */
    private String faceName;
    
    /**
     * 성공 응답 생성
     */
    public static FaceLoginResponse success(String userId, String userName, String accessToken, String refreshToken, String faceName) {
        return FaceLoginResponse.builder()
                .success(true)
                .message("페이스 로그인 성공")
                .userId(userId)
                .userName(userName)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .faceName(faceName)
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static FaceLoginResponse failure(String message) {
        return FaceLoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}