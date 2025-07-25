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
import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import com.test.seems.test.jpa.entity.ScaleAnalysisResultEntity;
import com.test.seems.test.jpa.repository.PersonalityTestResultRepository;
import com.test.seems.test.jpa.repository.PsychologicalTestResultRepository;
import com.test.seems.test.jpa.repository.ScaleAnalysisResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.test.seems.emotion.jpa.repository.EmotionRepository;
import com.test.seems.emotion.jpa.entity.Emotion;
import java.util.*;

@Slf4j
@Service
public class UserTaskStatusService {

    @Autowired
    private CounselingAnalysisSummaryRepository counselingAnalysisSummaryRepository;

    @Autowired
    private PersonalityTestResultRepository personalityTestResultRepository;

    @Autowired
    private PsychologicalTestResultRepository psychologicalTestResultRepository;

    @Autowired
    private ScaleAnalysisResultRepository scaleAnalysisResultRepository;

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private UserAnalysisSummaryRepository userAnalysisSummaryRepository;

    private final RestTemplate restTemplate;

    public UserTaskStatusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

        Optional<PersonalityTestResultEntity> personalityResultOpt = personalityTestResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (personalityResultOpt.isPresent()) {
            status.setPersonalityTestCompleted(1);
            log.info("성격 검사 결과 존재: {}", personalityResultOpt.get().getPersonalityId());
        } else {
            log.info("성격 검사 결과 없음.");
        }

        Optional<PsychologicalTestResultEntity> psychImageResultOpt = psychologicalTestResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (psychImageResultOpt.isPresent()) {
            status.setPsychImageTestCompleted(1);
            log.info("이미지 심리 검사 결과 존재: {}", psychImageResultOpt.get().getResultId());
        } else {
            log.info("이미지 심리 검사 결과 없음.");
        }

        Optional<ScaleAnalysisResultEntity> depressionResultOpt = scaleAnalysisResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "DEPRESSION_SCALE");
        if (depressionResultOpt.isPresent()) {
            status.setDepressionTestCompleted(1);
            log.info("우울증 검사 결과 존재: {}", depressionResultOpt.get().getResultId());
        } else {
            log.info("우울증 검사 결과 없음.");
        }

        Optional<ScaleAnalysisResultEntity> stressResultOpt = scaleAnalysisResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "STRESS_SCALE");
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
        Optional<PersonalityTestResultEntity> personalityResultOpt = personalityTestResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        log.info("DEBUG: personalityResultOpt.isPresent(): {}", personalityResultOpt.isPresent());
        Optional<PsychologicalTestResultEntity> psychImageResultOpt = psychologicalTestResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        log.info("DEBUG: psychImageResultOpt.isPresent(): {}", psychImageResultOpt.isPresent());
        Optional<ScaleAnalysisResultEntity> depressionResultOpt = scaleAnalysisResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "DEPRESSION_SCALE");
        log.info("DEBUG: depressionResultOpt.isPresent(): {}", depressionResultOpt.isPresent());
        Optional<ScaleAnalysisResultEntity> stressResultOpt = scaleAnalysisResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, "STRESS_SCALE");
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
        if (counselingSummaryOpt.isPresent()) {
            Map<String, Object> counselingMap = new HashMap<>();
            counselingMap.put("latestCounselingSummary", Map.of(
                "summaryContent", counselingSummaryOpt.get().getSummaryContent(),
                "topic", "N/A", // Assuming topic is not directly available or needs to be derived
                "method", "N/A", // Assuming method is not directly available or needs to be derived
                "startTime", counselingSummaryOpt.get().getCreatedAt().toString(), // Using createdAt as start time
                "endTime", counselingSummaryOpt.get().getCreatedAt().toString() // Using createdAt as end time
            ));
            resultsList.add(counselingMap);
        }

        // 2. Personality Test Result
        if (personalityResultOpt.isPresent()) {
            Map<String, Object> personalityMap = new HashMap<>();
            personalityMap.put("latestPersonalityResult", Map.of(
                "result", personalityResultOpt.get().getResult(),
                "description", personalityResultOpt.get().getDescription()
            ));
            resultsList.add(personalityMap);
        }

        // 3. Image Analysis Result
        if (psychImageResultOpt.isPresent()) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("latestImageResult", Map.of(
                "aiSentiment", psychImageResultOpt.get().getAiSentiment(),
                "aiSentimentScore", psychImageResultOpt.get().getAiSentimentScore(),
                "aiCreativityScore", psychImageResultOpt.get().getAiCreativityScore(),
                "aiInsightSummary", psychImageResultOpt.get().getAiInsightSummary(),
                "aiPerspectiveKeywords", psychImageResultOpt.get().getAiPerspectiveKeywords()
            ));
            resultsList.add(imageMap);
        }

        // 4. Depression Test Result
        if (depressionResultOpt.isPresent()) {
            Map<String, Object> depressionMap = new HashMap<>();
            depressionMap.put("latestDepressionResult", Map.of(
                "totalScore", depressionResultOpt.get().getTotalScore(),
                "interpretation", depressionResultOpt.get().getInterpretation(),
                "riskLevel", depressionResultOpt.get().getRiskLevel()
            ));
            resultsList.add(depressionMap);
        }

        // 5. Stress Test Result
        if (stressResultOpt.isPresent()) {
            Map<String, Object> stressMap = new HashMap<>();
            stressMap.put("latestStressResult", Map.of(
                "totalScore", stressResultOpt.get().getTotalScore(),
                "interpretation", stressResultOpt.get().getInterpretation(),
                "riskLevel", stressResultOpt.get().getRiskLevel()
            ));
            resultsList.add(stressMap);
        }

        requestData.put("results", resultsList);

        String pythonAiServerUrl = "http://localhost:5000/final-analysis"; // Python AI 서버 URL

        try {
            String aiResponse = restTemplate.postForObject(pythonAiServerUrl, requestData, String.class);
            log.info("DEBUG: Raw AI response from Python AI server: {}", aiResponse);

            // AI 응답이 JSON 문자열이므로, 이를 파싱하여 필요한 필드를 추출
            // Python 서버에서 ensure_ascii=False로 설정했으므로, 한글이 깨지지 않고 올 것입니다.
            Map<String, Object> parsedAiResponse = new ObjectMapper().readValue(aiResponse, new TypeReference<Map<String, Object>>() {});
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

            // aiResponse 대신 파싱된 객체에서 analysisComment를 가져옴
            userAnalysisSummary.setAnalysisComment((String) parsedAiResponse.get("aiInsightSummary"));
            userAnalysisSummary.setDominantEmotion((String) parsedAiResponse.get("dominantEmotion")); // 주요 감정 저장
// dominantEmotion이 있을 때 EMOTION_ID도 저장
            String dominantEmotion = (String) parsedAiResponse.get("dominantEmotion");
            if (dominantEmotion != null && !dominantEmotion.isEmpty()) {
                Emotion emotion = emotionRepository.findByEmotionName(dominantEmotion);
                if (emotion != null) {
                    userAnalysisSummary.setEmotionId(emotion.getEmotionId());
                } else {
                    userAnalysisSummary.setEmotionId(null); // 매칭되는 감정이 없으면 null
                }
            } else {
                userAnalysisSummary.setEmotionId(null);
            }

            userAnalysisSummary.setLastUpdated(new Date());
            userAnalysisSummary.setAnalysisCompleted(1);

            // individualResults를 JSON 문자열로 변환하여 저장
            ObjectMapper objectMapper = new ObjectMapper();
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
            userAnalysisSummaryRepository.flush(); // 변경 사항 즉시 반영

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
                ObjectMapper objectMapper = new ObjectMapper();
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
                .emotionId(entity.getEmotionId())
                .counselingSummaryId(entity.getCounselingSummaryId())
                .simulationResultId(entity.getSimulationResultId())
                .analysisComment(entity.getAnalysisComment())
                .analysisCompleted(entity.getAnalysisCompleted())
                .dominantEmotion(entity.getDominantEmotion()) // 주요 감정 설정
                .individualResults(individualResults) // DB에서 가져온 individualResults 설정
                .build();
    }
}