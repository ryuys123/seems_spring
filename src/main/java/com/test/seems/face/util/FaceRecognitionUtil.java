package com.test.seems.face.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaceRecognitionUtil {
    
    private final RestTemplate restTemplate;
    
    @Value("${deepface.service.url:http://localhost:5000}")
    private String deepfaceServiceUrl;
    
    /**
     * 얼굴 인식 수행
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @return 인식 결과 (사용자 ID 또는 null)
     */
    public String recognizeFace(String faceImageData) {
        try {
            String url = deepfaceServiceUrl + "/recognize";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_data", faceImageData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    return (String) result.get("user_id");
                } else {
                    log.warn("얼굴 인식 실패: {}", result.get("message"));
                    return null;
                }
            }
            
            log.error("얼굴 인식 API 호출 실패: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("얼굴 인식 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * 얼굴 등록
     * @param userId 사용자 ID
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @param faceName 페이스 이름
     * @return 등록 성공 여부
     */
    public boolean registerFace(String userId, String faceImageData, String faceName) {
        try {
            String url = deepfaceServiceUrl + "/register";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("image_data", faceImageData);
            requestBody.put("face_name", faceName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    log.info("얼굴 등록 성공: 사용자 {}, 페이스 {}", userId, faceName);
                    return true;
                } else {
                    log.warn("얼굴 등록 실패: {}", result.get("message"));
                    return false;
                }
            }
            
            log.error("얼굴 등록 API 호출 실패: {}", response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            log.error("얼굴 등록 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 얼굴 삭제
     * @param userId 사용자 ID
     * @param faceName 페이스 이름
     * @return 삭제 성공 여부
     */
    public boolean deleteFace(String userId, String faceName) {
        try {
            String url = deepfaceServiceUrl + "/delete";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("face_name", faceName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    log.info("얼굴 삭제 성공: 사용자 {}, 페이스 {}", userId, faceName);
                    return true;
                } else {
                    log.warn("얼굴 삭제 실패: {}", result.get("message"));
                    return false;
                }
            }
            
            log.error("얼굴 삭제 API 호출 실패: {}", response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            log.error("얼굴 삭제 중 오류 발생", e);
            return false;
        }
    }
}