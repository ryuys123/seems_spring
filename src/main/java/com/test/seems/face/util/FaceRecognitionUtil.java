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
     * 얼굴 인식 (로그인 시 - 얼굴만으로 사용자 찾기)
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @return 인식된 사용자 ID (인식 실패 시 null)
     */
    public String recognizeFace(String faceImageData) {
        try {
            String url = deepfaceServiceUrl + "/api/face/recognize";
            log.info("DeepFace 서비스 호출 시작: URL={}", url);
            log.info("얼굴 인식 요청 데이터 - faceImageData 길이: {}, null 여부: {}", 
                    faceImageData != null ? faceImageData.length() : "null", faceImageData == null);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_data", faceImageData);
            
            log.info("얼굴 인식 요청 본문: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            
            log.info("DeepFace 서비스 응답: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                
                if (success != null && success) {
                    String userId = (String) responseBody.get("user_id");
                    log.info("DeepFace 서비스에서 사용자 인식 성공: userId={}", userId);
                    return userId;
                } else {
                    log.warn("DeepFace 서비스에서 사용자 인식 실패");
                    return null;
                }
            }
            
            log.warn("DeepFace 서비스에서 예상치 못한 응답: status={}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("DeepFace 서비스 호출 중 오류 발생: error={}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 얼굴 등록 (중복 체크 포함)
     * @param userId 사용자 ID
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @param faceName 페이스 이름
     * @return 등록 성공 여부
     */
    public boolean registerFace(String userId, String faceImageData, String faceName) {
        try {
            String url = deepfaceServiceUrl + "/api/face/register";
            log.info("DeepFace 서비스 호출 시작: URL={}, userId={}, faceName={}", url, userId, faceName);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("image_data", faceImageData);
            requestBody.put("face_name", faceName);
            
            log.info("DeepFace 서비스 요청 데이터 - userId: {}, faceName: {}, imageData 길이: {}", 
                    userId, faceName, faceImageData != null ? faceImageData.length() : "null");
            log.info("DeepFace 서비스 요청 본문: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("DeepFace 서비스 요청 전송 중...");
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            log.info("DeepFace 서비스 응답 수신: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (success != null && success) {
                    log.info("얼굴 등록 성공: 사용자 {}, 페이스 {}", userId, faceName);
                    return true;
                } else {
                    String message = (String) result.get("message");
                    log.warn("얼굴 등록 실패: userId={}, faceName={}, message={}", userId, faceName, message);
                    return false;
                }
            }
            
            log.error("얼굴 등록 API 호출 실패: status={}, body={}", response.getStatusCode(), response.getBody());
            return false;
            
        } catch (Exception e) {
            log.error("얼굴 등록 중 오류 발생: userId={}, faceName={}, error={}", userId, faceName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 얼굴 중복 등록 체크
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @return 이미 등록된 사용자 ID, 없으면 null
     */
    public String checkFaceDuplicate(String faceImageData) {
        try {
            String url = deepfaceServiceUrl + "/api/face/check-duplicate";
            log.info("DeepFace 서비스 중복 체크 호출 시작: URL={}", url);
            log.info("중복 체크 요청 데이터 - faceImageData 길이: {}, null 여부: {}", 
                    faceImageData != null ? faceImageData.length() : "null", faceImageData == null);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_data", faceImageData);
            
            log.info("중복 체크 요청 본문: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            log.info("DeepFace 서비스 중복 체크 응답: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                Boolean exists = (Boolean) result.get("exists");
                
                if (exists != null && exists) {
                    String existingUserId = (String) result.get("user_id");
                    log.info("중복 얼굴 발견: 기존 사용자 {}", existingUserId);
                    return existingUserId;
                } else {
                    log.info("중복 얼굴 없음");
                    return null;
                }
            }
            
            log.warn("얼굴 중복 체크 API 호출 실패: status={}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("얼굴 중복 체크 중 오류 발생: error={}", e.getMessage(), e);
            return null;
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
            String url = deepfaceServiceUrl + "/api/face/delete";
            
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

    /**
     * 얼굴 인증 (로그인 시)
     * @param userId 사용자 ID
     * @param faceImageData Base64 인코딩된 얼굴 이미지
     * @return 인증 성공 여부
     */
    public boolean verifyFace(String userId, String faceImageData) {
        try {
            String url = deepfaceServiceUrl + "/api/face/verify";
            log.info("DeepFace 서비스 호출 시작: URL={}, userId={}", url, userId);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("image_data", faceImageData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            
            log.info("DeepFace 서비스 응답: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                return success != null && success;
            }
            
            log.warn("DeepFace 서비스에서 예상치 못한 응답: status={}", response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            log.error("DeepFace 서비스 호출 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }
}