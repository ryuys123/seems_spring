package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import com.test.seems.analysis.jpa.repository.UserAnalysisSummaryRepository;
import com.test.seems.simulation.jpa.entity.SimulationSettingEntity;
import com.test.seems.simulation.jpa.entity.SimulationUserResultEntity;
import com.test.seems.simulation.jpa.repository.SimulationSettingRepository;
import com.test.seems.simulation.jpa.repository.SimulationUserResultRepository;
import com.test.seems.simulation.model.dto.SimulationQuestion;
import com.test.seems.simulation.model.dto.SimulationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private static final int MAX_SIMULATION_QUESTIONS = 7; // 시뮬레이션 최대 질문 수

    private final UserAnalysisSummaryRepository userAnalysisSummaryRepository;
    private final AiGenerationService aiGenerationService;
    private final SimulationSettingRepository settingRepository;
    private final SimulationUserResultRepository userResultRepository;
    private final ObjectMapper objectMapper; // JSON 직렬화/역직렬화에 사용

    /**
     * 시뮬레이션을 시작하고 첫 질문을 AI로부터 생성받습니다.
     *
     * @param userId 시뮬레이션을 시작할 사용자의 ID
     * @return 첫 번째 시뮬레이션 질문 DTO
     * @throws RuntimeException 사용자 분석 결과가 없거나 시뮬레이션 시작에 필요한 데이터가 없을 경우
     */
    @Transactional
    public SimulationQuestion startSimulation(String userId) {
        log.info("Starting simulation for userId: {}", userId);

        // 사용자 종합 분석 요약 정보를 조회합니다.
        // 이 정보가 Python AI 서버의 첫 시뮬레이션 프롬프트에 사용됩니다.
        UserAnalysisSummaryEntity summary = userAnalysisSummaryRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("종합 분석 결과가 없는 사용자입니다: " + userId));

        // AI 서버에 첫 질문 생성을 요청합니다.
        // AiGenerationService.generateCustomSimulation은 이제 UserAnalysisSummaryEntity 자체를 넘겨
        // Python이 그 안의 데이터(analysisComment, stressScore, depressionScore, mbti, dominantEmotion 등)를 직접 파싱하도록 합니다.
        Map<String, Object> aiGeneratedStep = aiGenerationService.generateCustomSimulation(summary);

        // 새로운 시뮬레이션 설정(Setting)을 생성하고 저장합니다.
        SimulationSettingEntity setting = SimulationSettingEntity.builder()
                .userId(userId)
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .currentQuestionNumber(1) // 첫 질문이므로 1로 초기화
                .totalQuestionsCount(MAX_SIMULATION_QUESTIONS)
                // 초기 스트레스/우울감 점수를 Setting에 저장하여 추후 최종 분석에 활용
                .initialStressScore(summary.getStressScore())
                .initialDepressionScore(summary.getDepressionScore())
                .build();
        setting = settingRepository.save(setting);
        log.info("New simulation setting created with ID: {}", setting.getSettingId());

        // AI 응답을 DTO로 매핑하고 설정 ID와 시뮬레이션 종료 여부를 설정합니다.
        SimulationQuestion firstStepDto = mapAiResponseToDto(aiGeneratedStep);
        firstStepDto.setSettingId(setting.getSettingId());
        firstStepDto.setIsSimulationEnded(false);

        return firstStepDto;
    }

    /**
     * 시뮬레이션을 다음 단계로 진행하고 AI로부터 다음 질문을 생성받습니다.
     * 마지막 질문일 경우, 최종 분석을 수행하고 결과를 반환합니다.
     *
     * @param settingId 현재 진행 중인 시뮬레이션 설정 ID
     * @param history   지금까지의 대화 기록 (시뮬레이션 과정)
     * @param choiceText 사용자의 최근 선택 텍스트
     * @return 다음 질문 또는 최종 결과 (isSimulationEnded 필드로 구분)
     * @throws IllegalArgumentException 시뮬레이션 설정이 없거나 유효하지 않을 경우
     * @throws RuntimeException AI 서버 통신 또는 응답 처리 중 오류 발생 시
     */
    @Transactional
    public Map<String, Object> continueSimulation(Long settingId, List<Map<String, Object>> history, String choiceText) {
        log.info("Continuing simulation for settingId: {}, current history size: {}, choice: {}", settingId, history.size(), choiceText);

        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation setting not found with ID: " + settingId));

        // 사용자 종합 분석 요약 정보를 조회합니다.
        // 이 정보는 Python AI 서버의 다음 질문 생성 프롬프트에 사용됩니다.
        UserAnalysisSummaryEntity userSummary = userAnalysisSummaryRepository.findByUserId(setting.getUserId())
                .orElseThrow(() -> new RuntimeException("종합 분석 결과가 없는 사용자입니다: " + setting.getUserId()));

        setting.incrementCurrentQuestionNumber();
        settingRepository.save(setting);

        // 시뮬레이션이 최대 질문 수에 도달했는지 확인합니다.
        if (setting.getCurrentQuestionNumber() > MAX_SIMULATION_QUESTIONS) {
            log.info("Simulation for settingId: {} reached max questions ({}). Ending simulation.", settingId, MAX_SIMULATION_QUESTIONS);
            // 시뮬레이션 종료 시 최종 분석을 수행합니다.
            SimulationResult finalResult = analyzeAndSaveResult(settingId, history);
            Map<String, Object> response = new HashMap<>();
            response.put("isSimulationEnded", true);
            response.put("result", finalResult); // 최종 결과 DTO를 포함
            return response;
        }

        // --- AI 서버로 보낼 requestData Map 구성 ---
        Map<String, Object> requestDataForContinuation = new HashMap<>();
        requestDataForContinuation.put("history", history); // 현재까지의 대화 기록
        requestDataForContinuation.put("choiceText", choiceText); // 사용자의 최근 선택
        requestDataForContinuation.put("currentQuestionNum", setting.getCurrentQuestionNumber()); // 현재 질문 번호
        requestDataForContinuation.put("totalQuestions", MAX_SIMULATION_QUESTIONS); // 총 질문 수

        // 사용자 종합 분석 정보 (AI 프롬프트에서 활용)
        requestDataForContinuation.put("userInitialSummary", userSummary.getAnalysisComment());
        requestDataForContinuation.put("userImageSentiment", userSummary.getDominantEmotion());
        requestDataForContinuation.put("initialStressScore", setting.getInitialStressScore());
        requestDataForContinuation.put("initialDepressionScore", setting.getInitialDepressionScore());

        // AI 서버에 다음 질문 생성을 요청합니다.
        // AiGenerationService.generateCustomSimulationContinuation은 이제 Map<String, Object>를 파라미터로 받습니다.
        Map<String, Object> aiGeneratedStep = aiGenerationService.generateCustomSimulationContinuation(requestDataForContinuation);

        // AI 응답을 DTO로 매핑하고 설정 ID와 시뮬레이션 종료 여부를 설정합니다.
        SimulationQuestion nextStepDto = mapAiResponseToDto(aiGeneratedStep);
        nextStepDto.setSettingId(setting.getSettingId());
        nextStepDto.setIsSimulationEnded(false); // 시뮬레이션이 끝나지 않았음

        Map<String, Object> response = new HashMap<>();
        response.put("isSimulationEnded", false);
        response.put("nextQuestion", nextStepDto);

        return response;
    }

    /**
     * AI로부터 받은 응답 Map을 SimulationQuestion DTO로 매핑합니다.
     *
     * @param aiResponse AI 서버로부터 받은 응답 Map
     * @return 매핑된 SimulationQuestion DTO
     */
    private SimulationQuestion mapAiResponseToDto(Map<String, Object> aiResponse) {
        // AI 응답에서 'options' 리스트를 안전하게 가져옵니다.
        List<Map<String, Object>> optionsFromAi = (List<Map<String, Object>>) aiResponse.getOrDefault("options", Collections.emptyList());

        // 각 선택지 Map을 SimulationQuestion.ChoiceOption DTO로 변환합니다.
        List<SimulationQuestion.ChoiceOption> options = optionsFromAi.stream()
                .map(optMap -> SimulationQuestion.ChoiceOption.builder()
                        .text((String) optMap.get("text"))
                        .nextQuestionNumber((Integer) optMap.get("nextQuestionNumber"))
                        .build())
                .collect(Collectors.toList());

        // narrative와 변환된 options를 사용하여 SimulationQuestion DTO를 빌드합니다.
        return SimulationQuestion.builder()
                .questionText((String) aiResponse.get("narrative"))
                .options(options)
                .build();
    }

    /**
     * 시뮬레이션 종료 시 최종 분석을 수행하고 결과를 저장합니다.
     *
     * @param settingId 완료된 시뮬레이션 설정 ID
     * @param history   시뮬레이션의 전체 대화 기록
     * @return 최종 분석 결과 DTO
     * @throws IllegalArgumentException 시뮬레이션 설정이 없거나 유효하지 않을 경우
     * @throws RuntimeException AI 서버 통신 또는 응답 처리 중 오류 발생 시
     */
    @Transactional
    public SimulationResult analyzeAndSaveResult(Long settingId, List<Map<String, Object>> history) {
        // 이미 결과가 존재하는지 확인하여 중복 저장을 방지합니다.
        Optional<SimulationUserResultEntity> existingResult = userResultRepository.findBySettingId(settingId);
        if (existingResult.isPresent()) {
            log.warn("Result for settingId: {} already exists. Returning existing result.", settingId);
            return existingResult.get().toDto();
        }

        log.info("Analyzing and saving simulation result for settingId: {}", settingId);

        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation setting not found with ID: " + settingId));

        // 사용자 종합 분석 요약 정보를 조회합니다.
        UserAnalysisSummaryEntity userSummary = userAnalysisSummaryRepository.findByUserId(setting.getUserId())
                .orElseThrow(() -> new RuntimeException("종합 분석 결과가 없는 사용자입니다: " + setting.getUserId()));


        // --- AI 서버의 최종 분석 API로 보낼 requestData Map 구성 ---
        Map<String, Object> analysisRequestData = new HashMap<>();
        analysisRequestData.put("history", history); // 전체 대화 기록
        analysisRequestData.put("initialStressScore", setting.getInitialStressScore()); // 시뮬레이션 시작 시점의 스트레스 점수
        analysisRequestData.put("initialDepressionScore", setting.getInitialDepressionScore()); // 시뮬레이션 시작 시점의 우울감 점수

        // 사용자 종합 분석 정보 (AI 프롬프트에서 활용)
        analysisRequestData.put("userInitialSummary", userSummary.getAnalysisComment());
        analysisRequestData.put("userImageSentiment", userSummary.getDominantEmotion());


        // AI 서버에 최종 분석을 요청합니다.
        Map<String, Object> finalAnalysis = aiGenerationService.endCopingSimulation(analysisRequestData);

        // AI 분석 결과에서 필요한 정보들을 추출합니다.
        String resultSummary = (String) finalAnalysis.get("resultSummary");
        String resultTitle = (String) finalAnalysis.get("resultTitle");
        Integer estimatedFinalStressScore = (Integer) finalAnalysis.get("estimated_final_stress_score");
        Integer estimatedFinalDepressionScore = (Integer) finalAnalysis.get("estimated_final_depression_score");
        String positiveContributionFactors = (String) finalAnalysis.get("positive_contribution_factors");

        // 시뮬레이션 사용자 결과 엔티티를 생성하고 저장합니다.
        SimulationUserResultEntity userResultEntity = SimulationUserResultEntity.builder()
                .settingId(settingId)
                .resultTitle(resultTitle)
                .resultSummary(resultSummary)
                .initialStressScore(setting.getInitialStressScore())
                .initialDepressionScore(setting.getInitialDepressionScore())
                .estimatedFinalStressScore(estimatedFinalStressScore)
                .estimatedFinalDepressionScore(estimatedFinalDepressionScore)
                .positiveContributionFactors(positiveContributionFactors)
                .createdAt(LocalDateTime.now())
                .build();

        SimulationUserResultEntity savedResult = userResultRepository.save(userResultEntity);
        log.info("Simulation result saved to TB_SIMULATION_USER_RESULTS for settingId: {}", settingId);

        // 시뮬레이션 설정의 상태를 'COMPLETED'로 변경합니다.
        setting.setStatus("COMPLETED");
        settingRepository.save(setting);

        // 저장된 결과를 DTO로 변환하여 반환합니다.
        return savedResult.toDto();
    }

    /**
     * 특정 사용자 ID의 최신 완료된 시뮬레이션 결과를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 최신 시뮬레이션 결과 DTO (존재하지 않으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<SimulationResult> getLatestSimulationResult(String userId) {
        Optional<SimulationSettingEntity> latestSetting = settingRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED");

        if (latestSetting.isPresent()) {
            Long settingId = latestSetting.get().getSettingId();
            Optional<SimulationUserResultEntity> userResult = userResultRepository.findBySettingId(settingId);
            return userResult.map(SimulationUserResultEntity::toDto);
        }
        return Optional.empty();
    }

    /**
     * 진행 중인 시뮬레이션이 있는지 확인하고, 있다면 해당 시뮬레이션의 settingId를 반환합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 진행 중인 시뮬레이션의 settingId Map (존재하지 않으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> resumeSimulation(String userId) {
        log.info("Attempting to resume simulation for userId: {}", userId);
        Optional<SimulationSettingEntity> settingOptional = settingRepository.findByUserIdAndStatus(userId, "IN_PROGRESS");

        if (settingOptional.isPresent()) {
            SimulationSettingEntity setting = settingOptional.get();
            Map<String, Object> resumeInfo = new HashMap<>();
            resumeInfo.put("settingId", setting.getSettingId());
            return Optional.of(resumeInfo);
        }
        log.info("No IN_PROGRESS simulation found for userId: {}", userId);
        return Optional.empty();
    }

    /**
     * 특정 시뮬레이션 설정 ID에 해당하는 최종 결과 상세 정보를 조회합니다.
     *
     * @param settingId 조회할 시뮬레이션 설정 ID
     * @return 시뮬레이션 결과 상세 DTO (존재하지 않으면 Optional.empty())
     */
    // ✨ [수정 후] 반환 타입을 SimulationResult로 변경하고, toDto() 메서드를 사용하여 코드를 간결하게 만듭니다.
    public Optional<SimulationResult> getSimulationResultDetails(Long settingId) {
        log.info("Fetching simulation result details for settingId: {}", settingId);
        return userResultRepository.findBySettingId(settingId)
                .map(SimulationUserResultEntity::toDto);
    }

}