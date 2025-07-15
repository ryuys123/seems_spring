package com.test.seems.face.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "face")
@Getter
@Setter
public class FaceConfig {
    
    // DeepFace 모델 설정
    private String modelName = "VGG-Face"; // 기본 모델
    private String distanceMetric = "cosine"; // 거리 측정 방식
    private double similarityThreshold = 0.6; // 유사도 임계값
    
    // Python DeepFace 서비스 설정
    private String pythonServiceUrl = "http://localhost:5000"; // Python 서비스 URL
    private String faceRecognitionEndpoint = "/api/face/recognize"; // 얼굴인식 엔드포인트
    private String faceFeatureEndpoint = "/api/face/extract"; // 특징추출 엔드포인트
    
    // 이미지 처리 설정
    private int maxImageSize = 1024 * 1024; // 최대 이미지 크기 (1MB)
    private String[] allowedImageFormats = {"jpg", "jpeg", "png"}; // 허용된 이미지 형식
    
    // 보안 설정
    private boolean enableFaceLogin = true; // 페이스로그인 활성화 여부
    private int maxLoginAttempts = 5; // 최대 로그인 시도 횟수
    private long lockoutDuration = 300; // 계정 잠금 시간 (초)
}
// 페이스 로그인