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

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {

    @Value("${python.ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 맞춤형 시뮬레이션 (극복 시뮬레이션)의 첫 질문 생성을 위해 AI 서버에 요청합니다.
     * (기존 generateCustomSimulation에서 이름 변경 및 일반화)
     *
     * @param summary 사용자의 종합 분석 요약 엔티티
     * @return AI가 생성한 첫 질문 정보 맵
     */
    public Map<String, Object> generateCustomSimulation(UserAnalysisSummaryEntity summary) { // ✅ 메서드명은 그대로 유지
        String url = aiServerUrl + "/generate-custom-simulation"; // ✅ AI 서버 엔드포인트명 유지
        log.info("AI 맞춤형 시나리오 생성 요청. User: {}", summary.getUserId());

        // 종합 분석 요약 정보로 AI에게 보낼 프롬프트를 동적으로 생성
        String prompt = createPromptFromSummary(summary);

        // AI 서버에 보낼 요청 데이터 구성
        Map<String, String> requestBody = Map.of("prompt", prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

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
     *
     * @param summary 사용자의 종합 분석 요약 엔티티
     * @return 생성된 프롬프트 문자열
     */
    private String createPromptFromSummary(UserAnalysisSummaryEntity summary) {
        String basePrompt = "[역할 부여] 당신은 인지행동치료(CBT)에 기반한 심리 시뮬레이션 AI입니다.\n";
        basePrompt += "[사용자 정보]\n" + summary.getAnalysisComment() + "\n";

        Integer stressScore = summary.getStressScore();
        Integer depressionScore = summary.getDepressionScore();

        if ((depressionScore != null && depressionScore > 45) || (stressScore != null && stressScore > 90)) {
            basePrompt += "[시뮬레이션 목표] 사용자가 직장에서의 스트레스와 우울감을 인지하고, 현실적인 극복 방안을 탐색하며, 작은 성취를 통해 무력감을 극복하도록 돕는 '내 안의 비판자' 컨셉의 시뮬레이션을 생성하세요.";
        } else {
            basePrompt += "[시뮬레이션 목표] 사용자의 성격적 강점을 활용하여 긍정적인 성과를 내는 '나의 강점 활용하기' 컨셉의 시나리오를 생성하세요.";
        }

        basePrompt += "\n[JSON 출력 형식] { \"narrative\": \"...\", \"internalThought\": \"...\", \"options\": [ { \"text\": \"선택지 내용\", \"nextQuestionNumber\": 다음_질문_번호_또는_null } ] }"; // nextQuestionNumber 예시 포함

        return basePrompt;
    }

    /**
     * 시뮬레이션의 다음 질문 생성을 위해 AI 서버에 요청합니다.
     * (기존 generateCustomSimulationContinuation에서 이름 변경 및 일반화)
     *
     * @param prompt AI에게 보낼 프롬프트 문자열 (대화 기록, 현재 질문 번호 등 포함)
     * @return AI가 생성한 다음 질문 정보 맵
     */
    public Map<String, Object> generateCustomSimulationContinuation(String prompt) { // ✅ 메서드명은 그대로 유지
        String url = aiServerUrl + "/continue-coping-simulation"; // ✅ AI 서버 엔드포인트명 유지
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

    /**
     * 시뮬레이션의 최종 분석을 위해 AI 서버에 요청합니다.
     * (기존 endCopingSimulation에서 이름 변경 및 일반화)
     *
     * @param requestData 분석에 필요한 데이터 (history, initialStressScore, initialDepressionScore 등)
     * @return AI가 분석한 최종 결과 맵
     */
    public Map<String, Object> endCopingSimulation(Map<String, Object> requestData) { // ✅ 메서드명은 그대로 유지
        String url = aiServerUrl + "/end-coping-simulation"; // ✅ AI 서버 엔드포인트명 유지
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