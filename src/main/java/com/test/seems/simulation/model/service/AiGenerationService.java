package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.simulation.model.dto.SimulationQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {

    // application.properties에서 파이썬 AI 서버 URL을 불러옵니다.
    @Value("${python.ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI 서버에 시뮬레이션 질문 생성을 요청합니다.
     * Python의 /generate_simulation 엔드포인트를 호출합니다.
     */
    public Map<String, Object> generateSimulationContent(String scenarioName, String previousContext) {

        String url = aiServerUrl + "/generate_simulation";

        // AI 서버로 보낼 요청 데이터
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("scenarioName", scenarioName);
        requestBody.put("previousContext", previousContext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // AI 서버 호출
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 파이썬 서버가 반환한 JSON 문자열을 Map으로 변환
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                return result;
            }
        } catch (Exception e) {
            log.error("AI 시나리오 생성 요청 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * AI 서버에 성향 분석을 요청합니다.
     * Python의 /analyze_traits 엔드포인트를 호출합니다.
     */
    public Map<String, Object> analyzeUserTraits(List<Map<String, Object>> choices) {

        String url = aiServerUrl + "/analyze_traits";

        // AI 서버로 보낼 요청 데이터 (사용자의 선택 기록)
        Map<String, List<Map<String, Object>>> requestBody = new HashMap<>();
        requestBody.put("choices", choices);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, List<Map<String, Object>>>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // AI 서버 호출
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 파이썬 서버가 반환한 분석 결과 JSON을 Map으로 변환
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                return result;
            }
        } catch (Exception e) {
            log.error("AI 성향 분석 요청 실패: {}", e.getMessage());
        }
        return null;

    }
    // Map을 SimulationQuestion.Option으로 변환하는 정적 헬퍼 메서드
    public static SimulationQuestion.Option mapToOption(Map<String, Object> optionMap) {
        return SimulationQuestion.Option.builder()
                .text((String) optionMap.get("text"))
                .trait((String) optionMap.get("trait"))
                .nextNarrative((String) optionMap.get("nextNarrative"))
                .build();
    }

}