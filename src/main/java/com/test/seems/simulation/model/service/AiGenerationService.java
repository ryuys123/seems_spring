package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap; // Map.of 대신 HashMap 사용을 위해 추가

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {

    @Value("${python.api.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 맞춤형 시뮬레이션 (극복 시뮬레이션)의 첫 질문 생성을 위해 AI 서버에 요청합니다.
     *
     * @param summary 사용자의 종합 분석 요약 엔티티
     * @return AI가 생성한 첫 질문 정보 맵
     */
    public Map<String, Object> generateCustomSimulation(UserAnalysisSummaryEntity summary) {
        String url = aiServerUrl + "/generate-custom-simulation";
        log.info("AI 맞춤형 시나리오 생성 요청. User: {}", summary.getUserId());

        // Python AI 서버로 전송할 데이터를 Map으로 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("analysisComment", summary.getAnalysisComment());
        requestBody.put("stressScore", summary.getStressScore());
        requestBody.put("depressionScore", summary.getDepressionScore());
        requestBody.put("userImageSentiment", summary.getDominantEmotion()); // UserAnalysisSummaryEntity의 dominantEmotion 활용

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            log.info("AI 서버로부터 받은 원본 응답(맞춤형): {}", responseBody);
            if (responseBody == null) {
                throw new RuntimeException("AI 서버로부터 비어있는 응답을 받았습니다.");
            }
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("AI 맞춤형 시나리오 생성 요청 중 오류 발생", e);
            throw new RuntimeException("AI 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 종합 분석 데이터를 바탕으로 AI에게 보낼 프롬프트를 생성하는 헬퍼 메서드입니다.
     * 이 메서드는 이제 사용하지 않습니다. 대신 generateCustomSimulation 메서드에서 직접 필요한 데이터를 Python으로 전송합니다.
     *
     * @param summary 사용자의 종합 분석 요약 엔티티
     * @return 생성된 프롬프트 문자열
     */
    // @Deprecated // 이 메서드는 더 이상 사용되지 않음
    private String createPromptFromSummary(UserAnalysisSummaryEntity summary) {
        // 이 메서드는 이제 사용되지 않습니다.
        // Python API가 직접 데이터를 받아 프롬프트를 구성하도록 변경되었습니다.
        return ""; // 또는 UnsupportedOperationException을 던질 수도 있습니다.
    }


    /**
     * 시뮬레이션의 다음 질문 생성을 위해 AI 서버에 요청합니다.
     *
     * @param requestData AI에게 보낼 데이터 (대화 기록, 현재 질문 번호, 사용자 분석 정보 등 포함)
     * @return AI가 생성한 다음 질문 정보 맵
     */
    public Map<String, Object> generateCustomSimulationContinuation(Map<String, Object> requestData) {
        String url = aiServerUrl + "/continue-coping-simulation";
        log.info("AI 맞춤형 시나리오 이어하기 요청.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers); // Map<String, Object>를 그대로 사용

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("AI 서버로부터 비어있는 응답을 받았습니다.");
            }
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("AI 시나리오 이어하기 요청 중 오류 발생", e);
            throw new RuntimeException("AI 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 시뮬레이션의 최종 분석을 위해 AI 서버에 요청합니다.
     *
     * @param requestData 분석에 필요한 데이터 (history, initialStressScore, initialDepressionScore 등)
     * @return AI가 분석한 최종 결과 맵
     */
    public Map<String, Object> endCopingSimulation(Map<String, Object> requestData) {
        String url = aiServerUrl + "/end-coping-simulation";
        log.info("AI 맞춤형 시뮬레이션 최종 분석 요청.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("AI 서버로부터 비어있는 응답을 받았습니다.");
            }
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("AI 최종 분석 요청 중 오류 발생", e);
            throw new RuntimeException("AI 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }
}