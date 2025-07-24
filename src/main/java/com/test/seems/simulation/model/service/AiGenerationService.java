package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.simulation.model.dto.SimulationQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {

    @Value("${python.ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateSimulationContent(String scenarioName, String targetName, String situationDesc) {
        String url = aiServerUrl + "/generate_simulation";
        log.info("AI 시나리오 생성 요청. URL: {}", url);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("scenarioName", scenarioName);
        requestBody.put("targetName", targetName);
        requestBody.put("situationDesc", situationDesc);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
// 응답 본문을 문자열로 먼저 저장합니다.
            String responseBody = response.getBody();
            // ✨✨ 핵심 디버깅 코드 ✨✨
            // 파싱하기 전에 AI 서버가 보낸 원본 응답을 그대로 로그에 남깁니다.
            log.info("AI 서버로부터 받은 원본 응답 문자열: {}", responseBody);
            // 성공했지만 응답 본문이 없는 경우에 대한 방어 코드
            if (response.getBody() == null) {
                log.warn("AI 서버로부터 2xx 성공 응답을 받았으나 응답 본문이 비어있습니다.");
                throw new RuntimeException("AI 서버로부터 비어있는 응답을 받았습니다.");
            }

            return objectMapper.readValue(response.getBody(), Map.class);

        } catch (RestClientResponseException e) {
            // ✨ 핵심 변경점 1: AI 서버가 4xx, 5xx 에러를 보냈을 때 잡는 블록
            log.error("AI 서버가 오류를 응답했습니다. Status: {}, Response Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            // 원본 예외를 포함하여 던져야 추적이 용이합니다.
            throw new RuntimeException("AI 서버 통신 중 오류가 발생했습니다.", e);

        } catch (JsonProcessingException e) {
            // ✨ 핵심 변경점 2: JSON 파싱 오류를 잡는 블록
            log.error("AI 서버의 응답을 파싱하는 데 실패했습니다.", e);
            throw new RuntimeException("AI 서버 응답 파싱에 실패했습니다.", e);

        } catch (Exception e) {
            // ✨ 핵심 변경점 3: 그 외 네트워크 오류 등을 잡는 블록
            log.error("AI 시나리오 생성 요청 중 예측하지 못한 오류가 발생했습니다.", e);
            throw new RuntimeException("AI 서버 요청 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    public Map<String, Object> analyzeUserTraits(List<String> selectedTraits) {
        String url = aiServerUrl + "/analyze_traits";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("selectedTraits", selectedTraits);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.error("AI 성향 분석 요청 실패: {}", e.getMessage());
        }
        return null;
    }

    public static SimulationQuestion.ChoiceOption  mapToOption(Map<String, Object> optionMap) {
        return SimulationQuestion.ChoiceOption.builder()
                .text((String) optionMap.get("text"))
                .nextQuestionNumber((Integer) optionMap.get("nextQuestionNumber"))
                .build();
    }
}