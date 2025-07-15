package com.test.seems.face.util;

import com.test.seems.face.config.FaceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepFaceClient {

    private final FaceConfig faceConfig;
    private final RestTemplate restTemplate;

    /**
     * 얼굴인식 수행
     * @param imageBytes 입력 이미지 바이트 배열
     * @return 인식된 사용자 ID, 실패 시 null
     */
    public String recognizeFace(byte[] imageBytes) {
        try {
            String url = faceConfig.getPythonServiceUrl() + faceConfig.getFaceRecognitionEndpoint();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_data", java.util.Base64.getEncoder().encodeToString(imageBytes));
            requestBody.put("model_name", faceConfig.getModelName());
            requestBody.put("distance_metric", faceConfig.getDistanceMetric());
            requestBody.put("similarity_threshold", faceConfig.getSimilarityThreshold());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    String userId = (String) result.get("user_id");
                    log.info("DeepFace 얼굴인식 성공 - userId: {}", userId);
                    return userId;
                } else {
                    log.warn("DeepFace 얼굴인식 실패: {}", result.get("message"));
                    return null;
                }
            }
            
            log.error("DeepFace 서비스 응답 오류: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("DeepFace 얼굴인식 중 오류: ", e);
            return null;
        }
    }

    /**
     * 얼굴 특징값 추출
     * @param imageBytes 입력 이미지 바이트 배열
     * @return 얼굴 특징값 문자열
     */
    public String extractFaceFeatures(byte[] imageBytes) {
        try {
            String url = faceConfig.getPythonServiceUrl() + faceConfig.getFaceFeatureEndpoint();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_data", java.util.Base64.getEncoder().encodeToString(imageBytes));
            requestBody.put("model_name", faceConfig.getModelName());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    String features = (String) result.get("features");
                    log.info("DeepFace 특징추출 성공");
                    return features;
                } else {
                    log.warn("DeepFace 특징추출 실패: {}", result.get("message"));
                    return null;
                }
            }
            
            log.error("DeepFace 서비스 응답 오류: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("DeepFace 특징추출 중 오류: ", e);
            return null;
        }
    }
} 