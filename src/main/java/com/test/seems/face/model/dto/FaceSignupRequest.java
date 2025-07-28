package com.test.seems.face.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceSignupRequest {

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 사용자 이름
     */
    private String username;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 얼굴 이미지 데이터 (Base64 인코딩)
     */
    private String faceImageData;

    /**
     * 얼굴 이미지 데이터 (Base64 인코딩) - React 호환용
     */
    private String imageData;

    /**
     * 기존 사용자 여부 (true: 기존 사용자, false: 신규 사용자)
     */
    private Boolean isExistingUser;
}