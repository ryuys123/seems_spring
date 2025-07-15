package com.test.seems.face.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRegistrationRequest {
    
    /**
     * 사용자 ID
     */
    private String userId;
    
    /**
     * 얼굴 이미지 데이터 (Base64 인코딩)
     */
    private String faceImageData;
    
    /**
     * 페이스 이름
     */
    private String faceName;
} 