package com.test.seems.analysis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import com.test.seems.analysis.jpa.repository.UserAnalysisSummaryRepository;
import com.test.seems.analysis.model.UserAnalysisSummaryDto;
import com.test.seems.analysis.model.UserTaskStatus;
import com.test.seems.counseling.jpa.entity.CounselingAnalysisSummaryEntity;
import com.test.seems.counseling.jpa.repository.CounselingAnalysisSummaryRepository;
import com.test.seems.emotion.jpa.entity.Emotion;
import com.test.seems.emotion.jpa.repository.EmotionRepository;
import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import com.test.seems.test.jpa.entity.PsychologicalScaleResult;
import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import com.test.seems.test.jpa.repository.PersonalityResultRepository;
import com.test.seems.test.jpa.repository.PsychologicalImageResultRepository;
import com.test.seems.test.jpa.repository.PsychologicalScaleResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class UserTaskStatusService {

    @Autowired
    private CounselingAnalysisSummaryRepository counselingAnalysisSummaryRepository;

    @Autowired
    private PersonalityResultRepository personalityResultRepository; // PersonalityResultRepository는 유지

    // @Autowired
    // private PsychologicalTestResultRepository psychologicalTestResultRepository; // ✅ 이 필드 삭제
    @Autowired
    private PsychologicalImageResultRepository psychologicalImageResultRepository; // ✅ 이 필드 추가

    @Autowired
    private PsychologicalScaleResultRepository psychologicalScaleResultRepository;

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private UserAnalysisSummaryRepository userAnalysisSummaryRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UserTaskStatusService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public UserTaskStatus getUserTaskStatus(String userId) {
        log.info("getUserTaskStatus 호출됨. userId: {}", userId);
        UserTaskStatus status = new UserTaskStatus(userId);

        Optional<UserAnalysisSummaryEntity> userAnalysisSummaryOpt = userAnalysisSummaryRepository.findByUserId(userId);
        if (userAnalysisSummaryOpt.isPresent() && userAnalysisSummaryOpt.get().getAnalysisComment() != null && !userAnalysisSummaryOpt.get().getAnalysisComment().isEmpty()) {
            status.setAnalysisCompleted(1);
            log.info("최종 분석 결과 존재: {}", userAnalysisSummaryOpt.get().getUserSummaryId());
        } else {
            log.info("최종 분석 결과 없음.");
        }

        Optional<CounselingAnalysisSummaryEntity> counselingSummaryOpt = counselingAnalysisSummaryRepository.findTopBySession_User_UserIdOrderByCreatedAtDesc(userId);
        if (counselingSummaryOpt.isPresent()) {
            status.setCounselingCompleted(1);
            log.info("상담 결과 존재: {}", counselingSummaryOpt.get().getSummaryId());
        } else {
            log.info("상담 결과 없음.");
        }

        Optional<PersonalityTestResultEntity> personalityResultOpt = personalityResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (personalityResultOpt.isPresent()) {
            status.setPersonalityTestCompleted(1);
            log.info("성격 검사 결과 존재: {}", personalityResultOpt.get().getPersonalityId());
        } else {
            log.info("성격 검사 결과 없음.");
        }

        // Optional<PsychologicalTestResultEntity> psychImageResultOpt = psychologicalTestResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId); // ✅ 이 줄 삭제
        Optional<PsychologicalTestResultEntity> psychImageResultOpt = psychologicalImageResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId); // ✅ 이 줄 추가
        if (psychImageResultOpt.isPresent()) {
            status.setPsychImageTestCompleted(1);
            log.info("이미지 심리 검사 결과 존재: {}", psychImageResultOpt.get().getResultId());
        } else {
            log.info("이미지 심리 검사 결과 없음.");
        }

        Optional<PsychologicalScaleResult> depressionResultOpt = psychologicalScaleResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "DEPRESSION_SCALE");
        if (depressionResultOpt.isPresent()) {
            status.setDepressionTestCompleted(1);
            log.info("우울증 검사 결과 존재: {}", depressionResultOpt.get().getResultId());
        } else {
            log.info("우울증 검사 결과 없음.");
        }

        Optional<PsychologicalScaleResult> stressResultOpt = psychologicalScaleResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "STRESS_SCALE");
        if (stressResultOpt.isPresent()) {
            status.setStressTestCompleted(1);
            log.info("스트레스 검사 결과 존재: {}", stressResultOpt.get().getResultId());
        } else {
            log.info("스트레스 검사 결과 없음.");
        }

        log.info("최종 UserTaskStatus: 상담={}, 성격={}, 이미지={}, 우울증={}, 스트레스={}",
                status.getCounselingCompleted(), status.getPersonalityTestCompleted(),
                status.getPsychImageTestCompleted(), status.getDepressionTestCompleted(),
                status.getStressTestCompleted());

        return status;
    }

    public boolean areAllTasksCompleted(String userId) {
        UserTaskStatus status = getUserTaskStatus(userId);
        return status.getCounselingCompleted() == 1 &&
                status.getPersonalityTestCompleted() == 1 &&
                status.getPsychImageTestCompleted() == 1 &&
                status.getDepressionTestCompleted() == 1 &&
                status.getStressTestCompleted() == 1;
    }

    @Transactional
    public String performIntegratedAnalysis(String userId) {
        log.info("DEBUG: performIntegratedAnalysis 호출됨. userId: {}", userId);
        if (!areAllTasksCompleted(userId)) {
            return "모든 과제가 완료되지 않아 통합 분석을 시작할 수 없습니다.";
        }

        Optional<CounselingAnalysisSummaryEntity> counselingSummaryOpt = counselingAnalysisSummaryRepository.findTopBySession_User_UserIdOrderByCreatedAtDesc(userId);
        log.info("DEBUG: counselingSummaryOpt.isPresent(): {}", counselingSummaryOpt.isPresent());
        Optional<PersonalityTestResultEntity> personalityResultOpt = personalityResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        log.info("DEBUG: personalityResultOpt.isPresent(): {}", personalityResultOpt.isPresent());
        Optional<PsychologicalTestResultEntity> psychImageResultOpt = psychologicalImageResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        log.info("DEBUG: psychImageResultOpt.isPresent(): {}", psychImageResultOpt.isPresent());
        Optional<PsychologicalScaleResult> depressionResultOpt = psychologicalScaleResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "DEPRESSION_SCALE");
        log.info("DEBUG: depressionResultOpt.isPresent(): {}", depressionResultOpt.isPresent());
        Optional<PsychologicalScaleResult> stressResultOpt = psychologicalScaleResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "STRESS_SCALE");
        log.info("DEBUG: stressResultOpt.isPresent(): {}", stressResultOpt.isPresent());

        log.info("DEBUG: All Optional checks before final validation: counseling={}, personality={}, image={}, depression={}, stress={}",
                counselingSummaryOpt.isPresent(), personalityResultOpt.isPresent(), psychImageResultOpt.isPresent(),
                depressionResultOpt.isPresent(), stressResultOpt.isPresent());

        if (counselingSummaryOpt.isEmpty() || personalityResultOpt.isEmpty() || psychImageResultOpt.isEmpty() ||
                depressionResultOpt.isEmpty() || stressResultOpt.isEmpty()) {
            return "필요한 모든 분석 결과가 존재하지 않습니다. 데이터 누락.";
        }

        // Python AI 서버로 전송할 데이터 구성
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", userId);

        List<Map<String, Object>> resultsList = new ArrayList<>();

        // 1. Counseling Summary
        counselingSummaryOpt.ifPresent(summary -> {
            Map<String, Object> counselingMap = new HashMap<>();
            counselingMap.put("latestCounselingSummary", Map.of(
                    "summaryContent", summary.getSummaryContent(),
                    "topic", "N/A",
                    "method", "N/A",
                    "startTime", summary.getCreatedAt().toString(),
                    "endTime", summary.getCreatedAt().toString()
            ));
            resultsList.add(counselingMap);
        });

        // 2. Personality Test Result
        personalityResultOpt.ifPresent(result -> {
            Map<String, Object> personalityMap = new HashMap<>();
            personalityMap.put("latestPersonalityResult", Map.of(
                    "result", result.getResult(),
                    "description", result.getDescription()
            ));
            resultsList.add(personalityMap);
        });

        // 3. Image Analysis Result
        psychImageResultOpt.ifPresent(result -> {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("latestImageResult", Map.of(
                    "aiSentiment", result.getAiSentiment(),
                    "aiSentimentScore", result.getAiSentimentScore(),
                    "aiCreativityScore", result.getAiCreativityScore(),
                    "aiInsightSummary", result.getAiInsightSummary(),
                    "aiPerspectiveKeywords", result.getAiPerspectiveKeywords()
            ));
            resultsList.add(imageMap);
        });

        // 4. Depression Test Result
        depressionResultOpt.ifPresent(result -> {
            Map<String, Object> depressionMap = new HashMap<>();
            depressionMap.put("latestDepressionResult", Map.of(
                    "totalScore", result.getTotalScore(),
                    "interpretation", result.getInterpretation(),
                    "riskLevel", result.getRiskLevel()
            ));
            resultsList.add(depressionMap);
        });

        // 5. Stress Test Result
        stressResultOpt.ifPresent(result -> {
            Map<String, Object> stressMap = new HashMap<>();
            stressMap.put("latestStressResult", Map.of(
                    "totalScore", result.getTotalScore(),
                    "interpretation", result.getInterpretation(),
                    "riskLevel", result.getRiskLevel()
            ));
            resultsList.add(stressMap);
        });

        requestData.put("results", resultsList);

        String pythonAiServerUrl = "http://localhost:5000/final-analysis"; // Python AI 서버 URL

        try {
            String aiResponse = restTemplate.postForObject(pythonAiServerUrl, requestData, String.class);
            log.info("DEBUG: Raw AI response from Python AI server: {}", aiResponse);

            Map<String, Object> parsedAiResponse = objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});
            log.info("DEBUG: Parsed AI response from Python AI server: {}", parsedAiResponse);
            log.info("DEBUG: parsedAiResponse.get(\"individualResults\") is null: {}", parsedAiResponse.get("individualResults") == null);

            UserAnalysisSummaryEntity userAnalysisSummary = userAnalysisSummaryRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserAnalysisSummaryEntity newSummary = new UserAnalysisSummaryEntity();
                        newSummary.setUserId(userId);
                        return newSummary;
                    });

            userAnalysisSummary.setCounselingSummaryId(counselingSummaryOpt.get().getSummaryId());
            userAnalysisSummary.setPersonalityResultId(personalityResultOpt.get().getPersonalityId());
            userAnalysisSummary.setPsychoImageResultId(psychImageResultOpt.get().getResultId());
            userAnalysisSummary.setPsychoScaleResultId(depressionResultOpt.get().getResultId());

            Integer stressScoreFromAi = (Integer) parsedAiResponse.get("stressScore");
            Integer depressionScoreFromAi = (Integer) parsedAiResponse.get("depressionScore");

            userAnalysisSummary.setStressScore(stressScoreFromAi);
            userAnalysisSummary.setDepressionScore(depressionScoreFromAi);

            userAnalysisSummary.setAnalysisComment((String) parsedAiResponse.get("aiInsightSummary"));
            userAnalysisSummary.setDominantEmotion((String) parsedAiResponse.get("dominantEmotion"));

            String dominantEmotion = (String) parsedAiResponse.get("dominantEmotion");
            if (dominantEmotion != null && !dominantEmotion.isEmpty()) {
                Emotion emotion = emotionRepository.findByEmotionName(dominantEmotion);
                if (emotion != null) {
                    userAnalysisSummary.setEmotionId(emotion.getEmotionId());
                } else {
                    userAnalysisSummary.setEmotionId(null);
                }
            } else {
                userAnalysisSummary.setEmotionId(null);
            }

            userAnalysisSummary.setLastUpdated(new Date());
            userAnalysisSummary.setAnalysisCompleted(1);

            String individualResultsJsonString = objectMapper.writeValueAsString(parsedAiResponse.get("individualResults"));
            log.info("DEBUG: individualResultsJsonString to be saved: {}", individualResultsJsonString);
            try {
                List<Map<String, Object>> testParsed = objectMapper.readValue(individualResultsJsonString, new TypeReference<List<Map<String, Object>>>() {});
                log.info("DEBUG: Test parsing of individualResultsJsonString successful: {}", testParsed);
            } catch (Exception e) {
                log.error("DEBUG: Test parsing of individualResultsJsonString failed: {}", e.getMessage());
            }
            userAnalysisSummary.setIndividualResultsJson(individualResultsJsonString);

            userAnalysisSummaryRepository.save(userAnalysisSummary);
            userAnalysisSummaryRepository.flush();

            return "통합 분석이 성공적으로 완료되었습니다. 결과: " + parsedAiResponse.get("aiInsightSummary");

        } catch (Exception e) {
            log.error("통합 분석 중 오류 발생: {}", e.getMessage(), e);
            return "통합 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public UserAnalysisSummaryDto getFinalAnalysisResult(String userId) {
        UserAnalysisSummaryEntity entity = userAnalysisSummaryRepository.findByUserId(userId).orElse(null);
        if (entity == null) {
            return null;
        }

        log.info("DEBUG: individualResultsJson retrieved from DB: {}", entity.getIndividualResultsJson());

        List<Map<String, Object>> individualResults = null;
        if (entity.getIndividualResultsJson() != null && !entity.getIndividualResultsJson().isEmpty()) {
            try {
                individualResults = objectMapper.readValue(entity.getIndividualResultsJson(), new TypeReference<List<Map<String, Object>>>() {});
                log.info("DEBUG: Parsed individualResults from DB: {}", individualResults);
            } catch (Exception e) {
                log.error("Error parsing individualResultsJson from DB: {}", e.getMessage());
            }
        }

        return UserAnalysisSummaryDto.builder()
                .userSummaryId(entity.getUserSummaryId())
                .userId(entity.getUserId())
                .lastUpdated(entity.getLastUpdated())
                .psychoImageResultId(entity.getPsychoImageResultId())
                .personalityResultId(entity.getPersonalityResultId())
                .psychoScaleResultId(entity.getPsychoScaleResultId())
                .counselingSummaryId(entity.getCounselingSummaryId())
                .simulationResultId(entity.getSimulationResultId())
                .analysisComment(entity.getAnalysisComment())
                .analysisCompleted(entity.getAnalysisCompleted())
                .dominantEmotion(entity.getDominantEmotion())
                .stressScore(entity.getStressScore())
                .depressionScore(entity.getDepressionScore())
                .individualResults(individualResults)
                .build();
    }
}