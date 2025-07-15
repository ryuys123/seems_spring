package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.simulation.jpa.entity.*;
import com.test.seems.simulation.jpa.repository.*;
import com.test.seems.simulation.model.dto.Simulation; // Simulation DTO 사용
import com.test.seems.simulation.model.dto.SimulationQuestion; // SimulationQuestion DTO 사용
import com.test.seems.simulation.model.dto.SimulationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final ScenarioRepository scenarioRepository;
    private final SimulationSettingRepository settingRepository;
    private final SimulationQuestionRepository questionRepository;
    private final SimulationChoiceRepository choiceRepository;
    private final SimulationResultRepository resultRepository;

    // AiGenerationService 주입
    private final AiGenerationService aiGenerationService;
    private final ObjectMapper objectMapper;

    // ---------------------------------------------------
    // 1. 시나리오 목록 조회 (SimulationDTO 대신 Simulation 사용)
    // ---------------------------------------------------
    public List<Simulation> getActiveScenarios() {
        List<ScenarioEntity> entities = scenarioRepository.findByIsActive(1);
        return entities.stream()
                .map(ScenarioEntity::toDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------
    // 2. 시뮬레이션 시작 및 첫 질문 생성 (AI 연동)
    // ---------------------------------------------------
    @Transactional
    public SimulationQuestion startSimulation(Long scenarioId, String userId) {

        ScenarioEntity scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));

        SimulationSettingEntity setting = SimulationSettingEntity.builder()
                .scenarioId(scenarioId)
                .userId(userId)
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .build();
        setting = settingRepository.save(setting);

        // AI 서버에 첫 질문 생성 요청
        Map<String, Object> aiResponse = aiGenerationService.generateSimulationContent(scenario.getScenarioName(), null);

        if (aiResponse == null) {
            throw new RuntimeException("Failed to generate first question from AI.");
        }

        // AI 응답 파싱 및 엔티티 저장
        String questionText = (String) aiResponse.get("questionText");
        List<Map<String, Object>> options = (List<Map<String, Object>>) aiResponse.get("options");
        String choiceOptionsJson = "";
        try {
            choiceOptionsJson = objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize options.", e);
        }

        SimulationQuestionEntity firstQuestion = SimulationQuestionEntity.builder()
                .scenarioId(scenarioId)
                .questionNumber(1)
                .questionText(questionText)
                .choiceOptions(choiceOptionsJson)
                .build();
        questionRepository.save(firstQuestion);

        // 첫 질문 DTO(SimulationQuestion)로 변환하여 반환
        return SimulationQuestion.builder()
                .questionId(firstQuestion.getQuestionId())
                .scenarioId(firstQuestion.getScenarioId())
                .questionNumber(firstQuestion.getQuestionNumber())
                .questionText(firstQuestion.getQuestionText())
                .options(options.stream()
                        .map(AiGenerationService::mapToOption) // Map을 DTO.Option으로 변환
                        .collect(Collectors.toList()))
                .build();
    }

    // ---------------------------------------------------
    // 3. 시뮬레이션 진행 (사용자 선택 처리 및 다음 질문 생성)
    // ---------------------------------------------------
    @Transactional
    public SimulationQuestion processChoiceAndGenerateNextQuestion(
            Long settingId, Integer currentQuestionNumber, String choiceText, String selectedTrait) {

        // 1. 사용자의 선택을 TB_SIMULATION_CHOICES에 저장
        SimulationChoiceEntity choice = SimulationChoiceEntity.builder()
                .settingId(settingId)
                .questionNumber(currentQuestionNumber)
                .choiceText(choiceText)
                .selectedTrait(selectedTrait)
                .build();
        choiceRepository.save(choice);

        // 2. 현재 시나리오 및 진행 상황 정보 가져오기 (AI 요청에 필요)
        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("Setting not found"));
        ScenarioEntity scenario = scenarioRepository.findById(setting.getScenarioId())
                .orElseThrow(() -> new RuntimeException("Scenario not found"));

        // 3. AI 서버에 다음 질문 생성 요청
        Map<String, Object> aiResponse = aiGenerationService.generateSimulationContent(
                scenario.getScenarioName(),
                "사용자가 \"" + choiceText + "\"를 선택했습니다. (특성: " + selectedTrait + ")");

        if (aiResponse == null) {
            throw new RuntimeException("Failed to generate next question from AI.");
        }

        // 4. 다음 질문을 TB_SIMULATION_QUESTIONS에 저장
        Integer nextQuestionNumber = currentQuestionNumber + 1;
        String questionText = (String) aiResponse.get("questionText");
        List<Map<String, Object>> options = (List<Map<String, Object>>) aiResponse.get("options");
        String choiceOptionsJson = "";
        try {
            choiceOptionsJson = objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize options.", e);
        }

        SimulationQuestionEntity nextQuestion = SimulationQuestionEntity.builder()
                .scenarioId(setting.getScenarioId())
                .questionNumber(nextQuestionNumber)
                .questionText(questionText)
                .choiceOptions(choiceOptionsJson)
                .build();
        questionRepository.save(nextQuestion);

        // 5. 다음 질문 DTO(SimulationQuestion)로 변환하여 반환
        return SimulationQuestion.builder()
                .questionId(nextQuestion.getQuestionId())
                .scenarioId(nextQuestion.getScenarioId())
                .questionNumber(nextQuestion.getQuestionNumber())
                .questionText(nextQuestion.getQuestionText())
                .options(options.stream()
                        .map(AiGenerationService::mapToOption)
                        .collect(Collectors.toList()))
                .build();
    }

    // ---------------------------------------------------
    // 4. 시뮬레이션 결과 분석 및 저장 (AI 연동)
    // ---------------------------------------------------
    @Transactional
    public SimulationResultDTO analyzeAndSaveResult(Long settingId) {

        List<SimulationChoiceEntity> choices = choiceRepository.findBySettingIdOrderByQuestionNumberAsc(settingId);

        List<Map<String, Object>> choiceDataForAI = choices.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("choiceText", c.getChoiceText());
                    map.put("selectedTrait", c.getSelectedTrait());
                    return map;
                }).collect(Collectors.toList());

        // AI 서버에 성향 분석 요청
        Map<String, Object> analysisResultMap = aiGenerationService.analyzeUserTraits(choiceDataForAI);

        if (analysisResultMap == null) {
            throw new RuntimeException("Failed to get analysis result from AI.");
        }

        // TB_SIMULATION_RESULTS에 결과 저장
        SimulationResultEntity result = SimulationResultEntity.builder()
                .settingId(settingId)
                .resultSummary((String) analysisResultMap.get("resultSummary"))
                .personalityType((String) analysisResultMap.get("personalityType"))
                .createdAt(LocalDateTime.now())
                .build();
        resultRepository.save(result);

        // TB_SIMULATION_SETTINGS 상태를 'COMPLETED'로 변경
        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("Setting not found"));
        setting.setStatus("COMPLETED");
        settingRepository.save(setting);

        // 결과 DTO 반환
        return SimulationResultDTO.fromEntity(result);
    }

    // ---------------------------------------------------
    // 5. 중간 저장된 시뮬레이션 불러오기
    // ---------------------------------------------------
    @Transactional(readOnly = true)
    public Optional<Simulation> resumeSimulation(String userId) {

        Optional<SimulationSettingEntity> settingOptional = settingRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

        if (settingOptional.isPresent()) {
            SimulationSettingEntity setting = settingOptional.get();

            // DTO(Simulation)로 변환하여 반환
            return Optional.of(Simulation.fromSettingEntity(setting));
        }

        return Optional.empty();
    }
}