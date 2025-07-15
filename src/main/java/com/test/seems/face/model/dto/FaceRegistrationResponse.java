package com.test.seems.face.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRegistrationResponse {
    
    /**
     * 등록 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 페이스 로그인 ID
     */
    private Long faceLoginId;
    
    /**
     * 페이스 이름
     */
    private String faceName;
    
    /**
     * 성공 응답 생성
     */
    public static FaceRegistrationResponse success(Long faceLoginId, String faceName) {
        return FaceRegistrationResponse.builder()
                .success(true)
                .message("페이스 등록 성공")
                .faceLoginId(faceLoginId)
                .faceName(faceName)
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static FaceRegistrationResponse failure(String message) {
        return FaceRegistrationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 