package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import com.test.seems.analysis.jpa.repository.UserAnalysisSummaryRepository;
import com.test.seems.simulation.jpa.entity.SimulationSettingEntity;
import com.test.seems.simulation.jpa.entity.SimulationUserResultEntity;
import com.test.seems.simulation.jpa.repository.SimulationSettingRepository;
import com.test.seems.simulation.jpa.repository.SimulationUserResultRepository;
import com.test.seems.simulation.model.dto.SimulationQuestion; // SimulationQuestion DTO는 유지
import com.test.seems.simulation.model.dto.SimulationResult; // SimulationResult DTO는 유지
import com.test.seems.simulation.model.dto.SimulationResultDetails; // ✅ SimulationResultDetails DTO 임포트 추가
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

    private static final int MAX_SIMULATION_QUESTIONS = 7;

    private final UserAnalysisSummaryRepository userAnalysisSummaryRepository;
    private final AiGenerationService aiGenerationService;

    // --- 의존성 주입 (Repositories & ObjectMapper) ---
    private final SimulationSettingRepository settingRepository;
    private final SimulationUserResultRepository userResultRepository;
    private final ObjectMapper objectMapper;


    @Transactional
    public Map<String, Object> continueSimulation(Long settingId, List<Map<String, Object>> history, String choiceText) {
        log.info("Continuing simulation for settingId: {}, current history size: {}, choice: {}", settingId, history.size(), choiceText);

        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation setting not found with ID: " + settingId));

        setting.incrementCurrentQuestionNumber();
        settingRepository.save(setting);

        if (setting.getCurrentQuestionNumber() > MAX_SIMULATION_QUESTIONS) {
            log.info("Simulation for settingId: {} reached max questions ({}). Ending simulation.", settingId, MAX_SIMULATION_QUESTIONS);
            SimulationResult finalResult = analyzeAndSaveResult(settingId, history); // 이 finalResult가 120/95점을 포함하는 곳입니다.
            Map<String, Object> response = new HashMap<>();
            response.put("isSimulationEnded", true);
            response.put("result", finalResult); // 이 `result` 필드에 최종 결과가 담깁니다.
            return response;
        }

        String prompt = createContinuationPrompt(
                history,
                choiceText,
                setting.getCurrentQuestionNumber(),
                MAX_SIMULATION_QUESTIONS
        );

        Map<String, Object> aiGeneratedStep = aiGenerationService.generateCustomSimulationContinuation(prompt);

        SimulationQuestion nextStepDto = mapAiResponseToDto(aiGeneratedStep);
        nextStepDto.setSettingId(setting.getSettingId());
        nextStepDto.setIsSimulationEnded(false);

        Map<String, Object> response = new HashMap<>();
        response.put("isSimulationEnded", false);
        response.put("nextQuestion", nextStepDto);

        return response;
    }

    private String createContinuationPrompt(List<Map<String, Object>> history, String choiceText, int currentQuestionNum, int totalQuestions) {
        return String.format(
                "[역할 부여] 당신은 심리 시뮬레이션 AI입니다.\n" +
                        "[시뮬레이션 정보] 현재 %d번째 질문이며, 총 %d개의 질문으로 시뮬레이션이 종료됩니다.\n" +
                        "[지금까지의 대화 내용]\n%s\n" +
                        "[사용자의 최근 선택]\n\"%s\"\n" +
                        "[시뮬레이션 목표] 사용자의 선택에 적절히 반응하고, 원래 목표에 맞춰 다음 단계의 시나리오를 이어서 생성해주세요. " +
                        "**만약 현재 질문이 마지막 질문(%d번째 질문)이라면, 선택지(`options`)를 제공하지 않거나, 각 선택지의 `nextQuestionNumber`를 `null`로 설정해주세요.**\n" +
                        "**최종 질문(7번째 질문)에 대한 응답이라면, 시뮬레이션 전반의 대화 기록과 초기 스트레스/우울감 점수를 바탕으로 사용자의 스트레스/우울감 수준이 어떻게 변화했는지 추정하고, 어떤 긍정적인 행동이나 선택이 이에 기여했는지를 구체적으로 분석하여, `resultTitle`, `resultSummary`, `estimated_final_stress_score`, `estimated_final_depression_score`, `positive_contribution_factors` 필드를 포함한 최종 분석 JSON을 반환해주세요.**\n" +
                        "[JSON 출력 형식 (일반 질문)] {\"narrative\": \"다음 시나리오 설명\", \"options\": [{\"text\": \"선택지1\", \"nextQuestionNumber\": 다음_질문_번호_또는_null}, ...]}\n" +
                        "[JSON 출력 형식 (최종 질문 후 분석)] {\"resultTitle\": \"결과 제목\", \"resultSummary\": \"최종 요약\", \"estimated_final_stress_score\": 70, \"estimated_final_depression_score\": 40, \"positive_contribution_factors\": \"...\"}\n" +
                        "다음 단계의 JSON 응답을 생성해주세요:",
                currentQuestionNum, totalQuestions, history.toString(), choiceText, totalQuestions
        );
    }

    @Transactional
    public SimulationQuestion startSimulation(String userId) {
        log.info("Starting simulation for userId: {}", userId);

        UserAnalysisSummaryEntity summary = userAnalysisSummaryRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("종합 분석 결과가 없는 사용자입니다: " + userId));

        Integer initialStress = summary.getStressScore();
        Integer initialDepression = summary.getDepressionScore();

        Map<String, Object> aiGeneratedStep = aiGenerationService.generateCustomSimulation(summary);

        SimulationSettingEntity setting = SimulationSettingEntity.builder()
                .userId(userId)
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .currentQuestionNumber(1)
                .totalQuestionsCount(MAX_SIMULATION_QUESTIONS)
                .initialStressScore(initialStress)
                .initialDepressionScore(initialDepression)
                .build();
        setting = settingRepository.save(setting);
        log.info("New simulation setting created with ID: {}", setting.getSettingId());

        SimulationQuestion firstStepDto = mapAiResponseToDto(aiGeneratedStep);
        firstStepDto.setSettingId(setting.getSettingId());
        firstStepDto.setIsSimulationEnded(false);

        return firstStepDto;
    }

    private SimulationQuestion mapAiResponseToDto(Map<String, Object> aiResponse) {
        List<Map<String, Object>> optionsFromAi = (List<Map<String, Object>>) aiResponse.getOrDefault("options", Collections.emptyList());

        List<SimulationQuestion.ChoiceOption> options = optionsFromAi.stream()
                .map(optMap -> SimulationQuestion.ChoiceOption.builder()
                        .text((String) optMap.get("text"))
                        .nextQuestionNumber((Integer) optMap.get("nextQuestionNumber"))
                        .build())
                .collect(Collectors.toList());

        return SimulationQuestion.builder()
                .questionText((String) aiResponse.get("narrative"))
                .options(options)
                .build();
    }

    @Transactional
    public SimulationResult analyzeAndSaveResult(Long settingId, List<Map<String, Object>> history) {
        Optional<SimulationUserResultEntity> existingResult = userResultRepository.findBySettingId(settingId);
        if (existingResult.isPresent()) {
            log.warn("Result for settingId: {} already exists. Returning existing result.", settingId);
            return existingResult.get().toDto();
        }

        log.info("Analyzing and saving simulation result for settingId: {}", settingId);

        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation setting not found with ID: " + settingId));

        Map<String, Object> analysisRequestData = new HashMap<>();
        analysisRequestData.put("history", history);
        analysisRequestData.put("initialStressScore", setting.getInitialStressScore());
        analysisRequestData.put("initialDepressionScore", setting.getInitialDepressionScore());

        Map<String, Object> finalAnalysis = aiGenerationService.endCopingSimulation(analysisRequestData);

        String resultSummary = (String) finalAnalysis.get("resultSummary");
        String resultTitle = (String) finalAnalysis.get("resultTitle");

        Integer estimatedFinalStressScore = (Integer) finalAnalysis.get("estimated_final_stress_score");
        Integer estimatedFinalDepressionScore = (Integer) finalAnalysis.get("estimated_final_depression_score");
        String positiveContributionFactors = (String) finalAnalysis.get("positive_contribution_factors");

        SimulationUserResultEntity userResultEntity = SimulationUserResultEntity.builder()
                .settingId(settingId)
                .resultTitle(resultTitle)
                .resultSummary(resultSummary)
                .initialStressScore(setting.getInitialStressScore()) // 시작 스트레스 점수를 setting에서 가져옴
                .initialDepressionScore(setting.getInitialDepressionScore()) // 시작 우울감 점수를 setting에서 가져옴
                .estimatedFinalStressScore(estimatedFinalStressScore)
                .estimatedFinalDepressionScore(estimatedFinalDepressionScore)
                .positiveContributionFactors(positiveContributionFactors)
                .createdAt(LocalDateTime.now())
                .build();

        SimulationUserResultEntity savedResult = userResultRepository.save(userResultEntity);
        log.info("Simulation result saved to TB_SIMULATION_USER_RESULTS for settingId: {}", settingId);

        setting.setStatus("COMPLETED");
        settingRepository.save(setting);

        return savedResult.toDto(); // 이 toDto() 메서드가 반환하는 SimulationResult DTO에 120/95점 포함된 것으로 보입니다.
    }

    @Transactional(readOnly = true)
    public Optional<SimulationResult> getLatestSimulationResult(String userId) {
        Optional<SimulationSettingEntity> latestSetting = settingRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED");

        if (latestSetting.isPresent()) {
            Long settingId = latestSetting.get().getSettingId();
            Optional<SimulationUserResultEntity> userResult = userResultRepository.findBySettingId(settingId);
            return userResult.map(SimulationUserResultEntity::toDto); // 이 toDto() 메서드가 반환하는 SimulationResult DTO에 120/95점 포함된 것으로 보입니다.
        }
        return Optional.empty();
    }

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

    // ✅ SimulationResultDetails를 가져오는 메서드 추가
    @Transactional(readOnly = true)
    public Optional<SimulationResultDetails> getSimulationResultDetails(Long settingId) {
        log.info("Fetching simulation result details for settingId: {}", settingId);
        // SimulationUserResultRepository를 사용하여 settingId에 해당하는 최종 사용자 결과를 조회합니다.
        Optional<SimulationUserResultEntity> userResultEntityOptional = userResultRepository.findBySettingId(settingId);

        return userResultEntityOptional.map(userResultEntity -> {
            // 조회된 엔티티를 SimulationResultDetails DTO로 변환합니다.
            // 이 DTO의 필드명과 엔티티의 필드명이 일치해야 합니다.
            // 예를 들어, SimulationUserResultEntity에 resultTitle, resultSummary 등의 필드가 직접 매핑되어 있다면.
            return new SimulationResultDetails(
                    userResultEntity.getResultTitle(),
                    userResultEntity.getResultSummary(),
                    userResultEntity.getPositiveContributionFactors(),
                    userResultEntity.getInitialStressScore(),
                    userResultEntity.getEstimatedFinalStressScore(),
                    userResultEntity.getInitialDepressionScore(),
                    userResultEntity.getEstimatedFinalDepressionScore()
            );
        });
    }
}