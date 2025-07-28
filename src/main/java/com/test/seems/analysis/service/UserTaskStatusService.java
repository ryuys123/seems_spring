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
    // ObjectMapper는 final 필드로 초기화하는 것이 일반적입니다.
    private final ObjectMapper objectMapper; // ✨ final로 선언하고 생성자에서 초기화하도록 변경 (더 좋은 관례)


    // 생성자를 통해 RestTemplate과 ObjectMapper를 주입받습니다.
    public UserTaskStatusService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 특정 사용자의 심리 검사 및 상담 완료 상태를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return UserTaskStatus 객체 (각 과제 완료 여부 포함)
     */
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

    /**
     * 모든 필수 과제가 완료되었는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 모든 과제 완료 시 true, 아니면 false
     */
    public boolean areAllTasksCompleted(String userId) {
        UserTaskStatus status = getUserTaskStatus(userId);
        return status.getCounselingCompleted() == 1 &&
                status.getPersonalityTestCompleted() == 1 &&
                status.getPsychImageTestCompleted() == 1 &&
                status.getDepressionTestCompleted() == 1 &&
                status.getStressTestCompleted() == 1;
    }

    /**
     * 5가지 심리 검사 및 상담 결과를 통합하여 AI 분석을 수행하고 DB에 저장합니다.
     *
     * @param userId 통합 분석을 수행할 사용자 ID
     * @return 통합 분석 결과 요약 문자열 또는 오류 메시지
     */
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
        counselingSummaryOpt.ifPresent(summary -> {
            Map<String, Object> counselingMap = new HashMap<>();
            counselingMap.put("latestCounselingSummary", Map.of(
                    "summaryContent", summary.getSummaryContent(),
                    "topic", "N/A", // Assume N/A if not directly available or needs to be derived
                    "method", "N/A", // Assume N/A if not directly available or needs to be derived
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

            // AI 응답이 JSON 문자열이므로, 이를 파싱하여 필요한 필드를 추출
            Map<String, Object> parsedAiResponse = objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});
            log.info("DEBUG: Parsed AI response from Python AI server: {}", parsedAiResponse);
            // "individualResults" 필드는 Python에서 다시 넣어주므로, 여기서는 null 체크 필요 없음 (있다면 AI 로직 확인)
            // log.info("DEBUG: parsedAiResponse.get(\"individualResults\") is null: {}", parsedAiResponse.get("individualResults") == null);

            UserAnalysisSummaryEntity userAnalysisSummary = userAnalysisSummaryRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserAnalysisSummaryEntity newSummary = new UserAnalysisSummaryEntity();
                        newSummary.setUserId(userId);
                        return newSummary;
                    });

            // 각 검사의 ID를 UserAnalysisSummaryEntity에 저장 (Optional에서 값 가져오기)
            counselingSummaryOpt.ifPresent(summary -> userAnalysisSummary.setCounselingSummaryId(summary.getSummaryId()));
            personalityResultOpt.ifPresent(result -> userAnalysisSummary.setPersonalityResultId(result.getPersonalityId()));
            psychImageResultOpt.ifPresent(result -> userAnalysisSummary.setPsychoImageResultId(result.getResultId()));
            // 우울증 스케일 ID (stressResultOpt는 스트레스 스케일이므로, depressionResultOpt에서 가져옴)
            depressionResultOpt.ifPresent(result -> userAnalysisSummary.setPsychoScaleResultId(result.getResultId()));


            // AI 응답에서 stressScore와 depressionScore를 추출하여 저장
            Integer stressScoreFromAi = (Integer) parsedAiResponse.get("stressScore");
            Integer depressionScoreFromAi = (Integer) parsedAiResponse.get("depressionScore");

            userAnalysisSummary.setStressScore(stressScoreFromAi); // ✨ 스트레스 점수 저장
            userAnalysisSummary.setDepressionScore(depressionScoreFromAi); // ✨ 우울증 점수 저장

            // AI 응답에서 analysisComment, dominantEmotion 가져와 저장
            userAnalysisSummary.setAnalysisComment((String) parsedAiResponse.get("aiInsightSummary"));
            userAnalysisSummary.setDominantEmotion((String) parsedAiResponse.get("dominantEmotion"));

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
            // objectMapper는 이미 final로 선언되어 생성자에서 초기화되므로 여기서 다시 생성할 필요 없음
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

    /**
     * 사용자 종합 분석 결과를 조회합니다.
     * DB에 저장된 UserAnalysisSummaryEntity를 UserAnalysisSummaryDto로 변환하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @return UserAnalysisSummaryDto 또는 null
     */
    public UserAnalysisSummaryDto getFinalAnalysisResult(String userId) {
        UserAnalysisSummaryEntity entity = userAnalysisSummaryRepository.findByUserId(userId).orElse(null);
        if (entity == null) {
            return null;
        }

        log.info("DEBUG: individualResultsJson retrieved from DB: {}", entity.getIndividualResultsJson());

        List<Map<String, Object>> individualResults = null;
        if (entity.getIndividualResultsJson() != null && !entity.getIndividualResultsJson().isEmpty()) {
            try {
                // objectMapper는 이미 final로 선언되어 생성자에서 초기화되므로 여기서 다시 생성할 필요 없음
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
                .dominantEmotion(entity.getDominantEmotion()) // 주요 감정 설정
                // ✨ DB에서 가져온 stressScore와 depressionScore를 DTO에 설정
                .stressScore(entity.getStressScore())
                .depressionScore(entity.getDepressionScore())
                .individualResults(individualResults) // DB에서 가져온 individualResults 설정
                .build();
    }
}