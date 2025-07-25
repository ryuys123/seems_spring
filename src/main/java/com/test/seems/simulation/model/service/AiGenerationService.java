package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
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

    // ✨ 1. '맞춤형(극복) 시나리오' 생성을 위한 새로운 메서드 추가
    public Map<String, Object> generateCustomSimulation(UserAnalysisSummaryEntity summary) {
        String url = aiServerUrl + "/generate-custom-simulation"; // Python AI 서버의 새 엔드포인트
        log.info("AI 맞춤형 시나리오 생성 요청. User: {}", summary.getUserId());

        // 종합 분석 요약 정보로 AI에게 보낼 프롬프트를 동적으로 생성
        String prompt = createPromptFromSummary(summary);

        // AI 서버에 보낼 요청 데이터 구성
        Map<String, String> requestBody = Map.of("prompt", prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // API 호출 및 예외 처리
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

    // ✨ 2. 종합 분석 데이터를 바탕으로 동적 프롬프트를 생성하는 헬퍼 메서드
    private String createPromptFromSummary(UserAnalysisSummaryEntity summary) {
        String basePrompt = "[역할 부여] 당신은 인지행동치료(CBT)에 기반한 심리 시뮬레이션 AI입니다.\n";
        basePrompt += "[사용자 정보]\n" + summary.getAnalysisComment() + "\n";

        Integer depressionScore = parseScoreFromJson(summary.getIndividualResultsJson(), "latestDepressionResult");

        if (depressionScore != null && depressionScore > 45) { // 예: 45점 이상이면 '극복' 시나리오
            basePrompt += "[시뮬레이션 목표] 사용자가 자기 비판적 사고에 반박하고, 작은 성취를 통해 무력감을 극복하도록 돕는 '내 안의 비판자' 컨셉의 시나리오를 생성하세요.";
        } else { // 그 외 긍정적인 상태일 경우
            basePrompt += "[시뮬레이션 목표] 사용자의 성격적 강점을 활용하여 긍정적인 성과를 내는 '나의 강점 활용하기' 컨셉의 시나리오를 생성하세요.";
        }

        basePrompt += "\n[JSON 출력 형식] { \"narrative\": \"...\", \"internalThought\": \"...\", \"options\": [ ... ] }";

        return basePrompt;
    }

    // ✨ 1. '극복 시뮬레이션 이어하기'를 위한 새로운 메서드를 추가합니다.
    public Map<String, Object> generateCustomSimulationContinuation(String prompt) {
        // Python AI 서버의 '이어하기' 엔드포인트 주소
        String url = aiServerUrl + "/continue-coping-simulation";
        log.info("AI 맞춤형 시나리오 이어하기 요청.");

        Map<String, String> requestBody = Map.of("prompt", prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

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

    // JSON 문자열에서 특정 검사의 점수를 파싱하는 헬퍼 메서드
    private Integer parseScoreFromJson(String json, String resultKey) {
        if (json == null || json.isEmpty()) return null;
        try {
            List<Map<String, Map<String, Object>>> results = objectMapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Map<String, Object>> resultWrapper : results) {
                if (resultWrapper.containsKey(resultKey)) {
                    // 점수 값이 Double일 수 있으므로 Number로 받아 intValue()로 변환
                    return ((Number) resultWrapper.get(resultKey).get("totalScore")).intValue();
                }
            }
        } catch (Exception e) {
            log.error("JSON에서 점수 파싱 실패. Key: {}, JSON: {}", resultKey, json, e);
        }
        return null;
    }


    // --- 기존 메서드들은 그대로 유지 ---
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
            String responseBody = response.getBody();
            log.info("AI 서버로부터 받은 원본 응답 문자열: {}", responseBody);
            if (response.getBody() == null) {
                log.warn("AI 서버로부터 2xx 성공 응답을 받았으나 응답 본문이 비어있습니다.");
                throw new RuntimeException("AI 서버로부터 비어있는 응답을 받았습니다.");
            }
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (RestClientResponseException e) {
            log.error("AI 서버가 오류를 응답했습니다. Status: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 서버 통신 중 오류가 발생했습니다.", e);
        } catch (JsonProcessingException e) {
            log.error("AI 서버의 응답을 파싱하는 데 실패했습니다.", e);
            throw new RuntimeException("AI 서버 응답 파싱에 실패했습니다.", e);
        } catch (Exception e) {
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

    public static SimulationQuestion.ChoiceOption mapToOption(Map<String, Object> optionMap) {
        return SimulationQuestion.ChoiceOption.builder()
                .text((String) optionMap.get("text"))
                .nextQuestionNumber((Integer) optionMap.get("nextQuestionNumber"))
                .build();
    }

    // ✨ 1. '극복 시뮬레이션 최종 분석'을 위한 새로운 메서드를 추가합니다.
    public Map<String, Object> endCopingSimulation(List<Map<String, Object>> history) {
        // Python AI 서버의 '최종 분석' 엔드포인트 주소
        String url = aiServerUrl + "/end-coping-simulation";
        log.info("AI 맞춤형 시뮬레이션 최종 분석 요청.");

        // Python 서버에 보낼 요청 데이터 (전체 대화 기록)
        Map<String, Object> requestBody = Map.of("history", history);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

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