// src/main/java/com/test/seems/test/model/service/PsychologyService.java

package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PsychologyEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;

import com.test.seems.test.jpa.repository.PsychologyRepository;
import com.test.seems.test.jpa.repository.TestQuestionRepository;
import com.test.seems.test.jpa.repository.PsychologicalTestResultRepository;

import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.dto.PsychologicalAnswerRequest;
import com.test.seems.test.model.dto.PsychologicalTestResultResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient; // WebClient 임포트

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map; // JSON 응답 파싱용
import java.util.stream.Collectors;

@Service
public class PsychologyService {

    private final TestQuestionRepository testQuestionRepository;
    private final PsychologyRepository psychologyRepository;
    private final PsychologicalTestResultRepository psychologicalTestResultRepository;
    private final WebClient pythonAiWebClient;

    @Autowired
    public PsychologyService(TestQuestionRepository testQuestionRepository,
                             PsychologyRepository psychologyRepository,
                             PsychologicalTestResultRepository psychologicalTestResultRepository,
                             WebClient pythonAiWebClient) {
        this.testQuestionRepository = testQuestionRepository;
        this.psychologyRepository = psychologyRepository;
        this.psychologicalTestResultRepository = psychologicalTestResultRepository;
        this.pythonAiWebClient = pythonAiWebClient;
    }

    /**
     * 역할: DB에서 'PSYCHOLOGICAL' 타입 중 'IMAGE_BASED' 카테고리의 문항(이미지 정보와 질문 텍스트) 하나를 랜덤으로 선택하여 반환합니다.
     * @return TestQuestion (랜덤 이미지 문항 DTO), 해당하는 문항이 없을 경우 null 반환
     */
    @Transactional(readOnly = true)
    public TestQuestion getRandomImageQuestion() {
        List<TestQuestionEntity> psychologicalQuestions = testQuestionRepository.findByTestType("PSYCHOLOGICAL");

        List<TestQuestionEntity> imageBasedQuestions = psychologicalQuestions.stream()
                .filter(q -> "IMAGE_BASED".equals(q.getCategory()))
                .collect(Collectors.toList());

        if (imageBasedQuestions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        TestQuestionEntity randomQuestionEntity = imageBasedQuestions.get(random.nextInt(imageBasedQuestions.size()));

        return randomQuestionEntity.toDto();
    }

    /**
     * 역할: 사용자가 제출한 심리 검사 답변(느낀 점 텍스트)을 저장하고,
     * 이 텍스트를 Python AI 모델로 분석 요청 후 결과를 DB에 저장하고 DTO 형태로 반환합니다.
     * @param answerRequest 사용자의 답변 요청 DTO (PsychologicalAnswerRequest)
     * @return PsychologicalTestResultResponse (심리 분석 결과 DTO)
     */
    @Transactional
    public PsychologicalTestResultResponse submitAnswerAndAnalyze(PsychologicalAnswerRequest answerRequest) {
        // 1. 사용자의 답변(느낀 점 텍스트)을 TB_PSYCHOLOGICAL_TEST_ANSWERS 테이블에 저장합니다.
        PsychologyEntity answerEntity = answerRequest.toEntity();
        psychologyRepository.save(answerEntity);

        // 2. Python AI 서버로 분석 요청을 보냅니다.
        String userText = answerRequest.getUserResponseText();
        Long questionId = answerRequest.getQuestionId();
        String userId = answerRequest.getUserId();

        // Python AI 서버에 보낼 요청 본문 (JSON)
        Map<String, Object> aiRequestBody = Map.of(
                "userId", userId,
                "questionId", questionId,
                "userResponseText", userText
        );

        // WebClient를 사용하여 Python AI 서버 API 호출
        PsychologicalTestResultResponse aiResponseDto;
        try {
            // /analyze-psychology 엔드포인트로 POST 요청을 보냅니다.
            aiResponseDto = pythonAiWebClient.post()
                    .uri("/analyze-psychology")
                    .bodyValue(aiRequestBody)
                    .retrieve()
                    .bodyToMono(PsychologicalTestResultResponse.class)
                    .block();

            // --- AI 분석 응답 DTO 확인 (디버깅용) ---
            System.out.println("--- AI 분석 응답 DTO 확인 ---");
            System.out.println("aiInsightSummary: " + aiResponseDto.getAiInsightSummary());
            System.out.println("aiSentiment: " + aiResponseDto.getAiSentiment());
            System.out.println("-----------------------------");

            if (aiResponseDto == null) {
                throw new RuntimeException("Python AI 서버로부터 유효한 응답을 받지 못했습니다.");
            }
        } catch (Exception e) {
            // AI 서버 통신 실패 시 오류 처리 또는 기본값 설정
            System.err.println("Python AI 서버 통신 중 오류 발생: " + e.getMessage());
            aiResponseDto = PsychologicalTestResultResponse.builder()
                    .userId(userId)
                    .questionId(questionId)
                    .rawResponseText(userText)
                    .aiSentiment("분석불가")
                    .aiSentimentScore(0.0)
                    .aiCreativityScore(0.0)
                    .aiPerspectiveKeywords("오류")
                    .aiInsightSummary("AI 분석 서버와 통신할 수 없습니다. (오류: " + e.getMessage() + ")")
                    .suggestions("AI 서버가 정상적으로 작동하는지 확인해주세요.")
                    .testDateTime(LocalDateTime.now())
                    .build();
        }

        // 3. AI 분석 결과를 심리 분석 결과 테이블(TB_PSYCHOLOGICAL_ANALYSIS)에 저장합니다.
        PsychologicalTestResultEntity resultEntity = PsychologicalTestResultEntity.builder()
                .userId(aiResponseDto.getUserId())
                .questionId(aiResponseDto.getQuestionId())
                .rawResponseText(aiResponseDto.getRawResponseText())
                .aiSentiment(aiResponseDto.getAiSentiment())
                .aiSentimentScore(aiResponseDto.getAiSentimentScore())
                .aiCreativityScore(aiResponseDto.getAiCreativityScore())
                .aiPerspectiveKeywords(aiResponseDto.getAiPerspectiveKeywords())
                .aiInsightSummary(aiResponseDto.getAiInsightSummary())
                .suggestions(aiResponseDto.getSuggestions())
                .createdAt(LocalDateTime.now())
                .build();

        resultEntity = psychologicalTestResultRepository.save(resultEntity); // DB에 저장하고 ID 포함된 엔티티 받기

        // 4. 저장된 엔티티를 클라이언트로 보낼 최종 DTO로 변환하여 반환합니다.
        return resultEntity.toDto();
    }

    /**
     * 역할: 특정 심리 검사 결과(리포트)를 DB에서 조회하여 DTO 형태로 반환합니다.
     * @param resultId 조회할 결과의 고유 ID
     * @return PsychologicalTestResultResponse (심리 분석 결과 DTO), 결과가 없을 경우 RuntimeException 발생
     */
    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getPsychologicalTestResult(Long resultId) {
        PsychologicalTestResultEntity resultEntity = psychologicalTestResultRepository.findByResultId(resultId)
                .orElseThrow(() -> new RuntimeException("심리 검사 결과를 찾을 수 없습니다. (ID: " + resultId + ")"));
        return resultEntity.toDto();
    }

    // ⭐️ 추가된 메서드: 사용자의 가장 최근 심리 분석 결과를 조회합니다.
    /**
     * 역할: 특정 사용자의 가장 최근 심리 분석 결과를 DB에서 조회하여 DTO 형태로 반환합니다.
     * @param userId 조회할 사용자의 고유 ID
     * @return PsychologicalTestResultResponse (가장 최근 결과 DTO), 없을 경우 null 반환
     */
    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getLatestPsychologicalTestResultByUserId(String userId) {

        // Repository에서 가장 최근 결과를 조회합니다.
        PsychologicalTestResultEntity resultEntity = psychologicalTestResultRepository.findTop1ByUserIdOrderByCreatedAtDesc(userId)
                .orElse(null); // 결과가 없을 경우 null 반환

        if (resultEntity == null) {
            return null;
        }

        // 엔티티를 DTO로 변환하여 반환합니다.
        return resultEntity.toDto();
    }
}