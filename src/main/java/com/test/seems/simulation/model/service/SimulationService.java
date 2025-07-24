package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.simulation.jpa.entity.*;
import com.test.seems.simulation.jpa.repository.*;
import com.test.seems.simulation.model.dto.Simulation;
import com.test.seems.simulation.model.dto.SimulationQuestion;
import com.test.seems.simulation.model.dto.SimulationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 수정: lombok.extern.slf4j.Slf4j로 변경 (44j -> 4j)
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    // --- 의존성 주입 (Repositories & ObjectMapper) ---
    private final ScenarioRepository scenarioRepository;
    private final SimulationSettingRepository settingRepository;
    private final SimulationQuestionRepository questionRepository;
    private final SimulationChoiceRepository choiceRepository;
    private final SimulationResultRepository resultRepository; // 템플릿용
    private final SimulationUserResultRepository userResultRepository; // 사용자 결과 저장용
    private final ObjectMapper objectMapper;


    /**
     * ✅ [새로운 메서드] 활성화된 모든 시뮬레이션 목록을 가져옵니다.
     * (오늘의 DAILY 시뮬레이션 + 활성화된 OVERCOMING 시뮬레이션)
     */
    public List<Simulation> getActiveSimulations() {
        List<ScenarioEntity> activeScenarios = new ArrayList<>();

        // 1. 오늘의 DAILY 시뮬레이션 추가
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        scenarioRepository.findByDayOfWeekAndIsActive(today, 1)
                .ifPresent(activeScenarios::add); // 존재하면 리스트에 추가

        // 2. 활성화된 OVERCOMING 시뮬레이션 목록 추가
        List<ScenarioEntity> overcomingScenarios = scenarioRepository
                .findBySimulationTypeAndIsActive(ScenarioEntity.SimulationType.OVERCOMING, 1);
        activeScenarios.addAll(overcomingScenarios);

        // 3. DTO 리스트로 변환하여 반환
        return activeScenarios.stream()
                .map(ScenarioEntity::toDto)
                .collect(Collectors.toList());
    }
    /**
     * ✅ [신규] 오늘의 요일에 해당하는 활성화된 시뮬레이션 정보를 가져옵니다.
     * 컨트롤러에서 이 메서드를 호출하여 사용자에게 오늘의 시뮬레이션을 보여줍니다.
     */

    public Optional<Simulation> getTodaysSimulation() {
        DayOfWeek today = LocalDate.now().getDayOfWeek(); // 오늘 요일 가져오기 (TUESDAY)
        log.info("Finding simulation for today: {}", today);

        // 오늘 요일에 해당하고(findByDayOfWeek), 활성화된(isActive=1) 시나리오 조회
        return scenarioRepository.findByDayOfWeekAndIsActive(today, 1)
                .map(ScenarioEntity::toDto); // Entity를 DTO로 변환하여 반환
    }

    /**
     * 시뮬레이션을 시작합니다.
     * 사용자가 '시작하기'를 누르면 이 메서드가 호출됩니다.
     */
    @Transactional
    public SimulationQuestion startSimulation(Long scenarioId, String userId) {
        log.info("Starting simulation for userId: {}, scenarioId: {}", userId, scenarioId);

        // 1. 시나리오 존재 여부 확인
        scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found with ID: " + scenarioId));

        // 2. 이전에 진행 중이던 시뮬레이션이 있으면 'CANCELLED' 처리
        settingRepository.findByUserIdAndStatus(userId, "IN_PROGRESS")
                .ifPresent(s -> {
                    s.setStatus("CANCELLED");
                    settingRepository.save(s);
                    log.info("Previous IN_PROGRESS simulation for user {} was cancelled.", userId);
                });

        // 3. 새로운 시뮬레이션 진행 정보(Setting) 생성 및 저장
        SimulationSettingEntity setting = SimulationSettingEntity.builder()
                .userId(userId)
                .scenarioId(scenarioId)
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .build();
        setting = settingRepository.save(setting);
        log.info("New simulation setting created with ID: {}", setting.getSettingId());

        // 4. 해당 시나리오의 첫 번째 질문(questionNumber=1) 조회
        SimulationQuestionEntity firstQuestionEntity = questionRepository
                .findFirstByScenarioIdOrderByQuestionNumberAsc(scenarioId) // 가장 낮은 번호의 질문을 찾도록 변경
                .orElseThrow(() -> new IllegalArgumentException("First question not found for scenario ID: " + scenarioId));

        // 5. DTO로 변환하여 반환
        SimulationQuestion firstQuestionDTO = convertToQuestionDTO(firstQuestionEntity);
        firstQuestionDTO.setSettingId(setting.getSettingId());
        firstQuestionDTO.setIsSimulationEnded(false); // 첫 질문이므로 종료 아님

        return firstQuestionDTO;
    }

    /**
     * 사용자의 선택을 처리하고, 다음 질문을 반환합니다.
     */
    @Transactional
    public SimulationQuestion processChoiceAndGenerateNextQuestion(
            Long settingId, Integer currentQuestionNumber, String choiceText, String selectedTrait) {

        log.info("Processing choice for settingId: {}, QNo: {}, Choice: {}, Trait: {}",
                settingId, currentQuestionNumber, choiceText, selectedTrait);

        // 1. 진행 정보(Setting) 조회
        SimulationSettingEntity setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation setting not found with ID: " + settingId));

        // 2. 사용자의 선택 저장
        SimulationChoiceEntity choice = SimulationChoiceEntity.builder()
                .settingId(settingId)
                .questionNumber(currentQuestionNumber)
                .choiceText(choiceText)
                .selectedTrait(selectedTrait)
                .build();
        choiceRepository.save(choice);
        log.info("User choice saved: {}", choiceText);

        // 3. 현재 질문 정보 조회 (다음 질문 번호를 얻기 위함)
        SimulationQuestionEntity currentQuestionEntity = questionRepository
                .findByScenarioIdAndQuestionNumber(setting.getScenarioId(), currentQuestionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Current question not found"));

        SimulationQuestion currentQuestionDTO = convertToQuestionDTO(currentQuestionEntity);
        // 4. 선택지에 명시된 다음 질문 번호(nextQuestionNumber) 찾기
        Integer nextQuestionNum = currentQuestionDTO.getOptions().stream()
                .filter(option -> choiceText.equals(option.getText())) // null 안전을 위해 choiceText를 기준으로 비교
                .findFirst() // 선택지 객체를 먼저 찾습니다.
                .map(SimulationQuestion.ChoiceOption::getNextQuestionNumber) // 찾은 객체에서 다음 질문 번호를 꺼냅니다.
                .orElse(null);

        // 5. 시뮬레이션 종료 조건 확인 (다음 질문이 없거나, 마지막 질문인 경우)
        if (nextQuestionNum == null) {
            log.info("Simulation for settingId: {} has ended.", settingId);
            setting.setStatus("COMPLETED"); // 상태를 '완료'로 변경
            settingRepository.save(setting);
            // 초기 코드에서는 finalResultPersonalityType을 DTO에 담지 않았습니다.
            // 필요하다면, 이 부분을 복원하기 전의 DTO 정의에 맞춰 조정해야 합니다.
            return SimulationQuestion.builder().isSimulationEnded(true).settingId(settingId).build();
        }

        // 6. 다음 질문 조회 및 반환
        SimulationQuestionEntity nextQuestionEntity = questionRepository
                .findByScenarioIdAndQuestionNumber(setting.getScenarioId(), nextQuestionNum)
                .orElseThrow(() -> new IllegalArgumentException("Next question not found: " + nextQuestionNum));

        SimulationQuestion nextQuestionDTO = convertToQuestionDTO(nextQuestionEntity);
        nextQuestionDTO.setSettingId(setting.getSettingId());
        nextQuestionDTO.setIsSimulationEnded(false); // 아직 종료 아님

        return nextQuestionDTO;
    }

    /**
     * ✅ [초기 상태] 시뮬레이션 결과를 분석하고 저장합니다.
     * 이 메서드는 settingId만 받아서 SimulationChoiceEntity를 통해 성향을 분석하고,
     * UNKNOWN 템플릿으로의 폴백 로직을 포함합니다.
     */
    @Transactional
    public SimulationResult analyzeAndSaveResult(Long settingId) {
        // 1. [중복 방지] 이미 사용자 결과가 있는지 먼저 확인
        Optional<SimulationUserResultEntity> existingResult = userResultRepository.findBySettingId(settingId);
        if (existingResult.isPresent()) {
            log.warn("Result for settingId: {} already exists. Returning existing result.", settingId);
            return existingResult.get().toDto();
        }

        log.info("Analyzing and saving result for settingId: {}", settingId);

        // 2. 성향 계산 (이전 로직 복원)
        List<SimulationChoiceEntity> choices = choiceRepository.findBySettingIdOrderByQuestionNumberAsc(settingId);
        String dominantTrait = determineDominantTrait(choices); // 성향 분석 메서드 다시 사용

        // 3. 템플릿 조회 (기존 resultRepository 사용)
        SimulationResultEntity resultTemplate = resultRepository
                .findByResultTypeAndPersonalityType("TEMPLATE", dominantTrait)
                .orElseGet(() -> resultRepository.findByResultTypeAndPersonalityType("TEMPLATE", "UNKNOWN")
                        .orElseThrow(() -> new IllegalStateException("Default 'UNKNOWN' template not found in DB!")));

        // 4. 조회한 템플릿으로 사용자 결과(UserResult) 엔티티 생성
        SimulationUserResultEntity userResultEntity = SimulationUserResultEntity.builder()
                .settingId(settingId)
                .personalityType(dominantTrait)
                .resultTitle(resultTemplate.getResultTitle())
                .resultSummary(resultTemplate.getResultSummary())
                .createdAt(LocalDateTime.now())
                .build();

        // 5. 생성된 사용자 결과를 새 테이블에 저장 (새로운 userResultRepository 사용)
        SimulationUserResultEntity savedResult = userResultRepository.save(userResultEntity);
        log.info("User simulation result saved to TB_SIMULATION_USER_RESULTS for settingId: {}", settingId);

        // 6. DTO로 변환하여 반환
        return savedResult.toDto();
    }

    /**
     * ✅ [초기 상태] 진행 중인 시뮬레이션이 있는지 확인하고 정보를 반환합니다.
     */
    public Optional<Simulation> resumeSimulation(String userId) {
        log.info("Attempting to resume simulation for userId: {}", userId);
        Optional<SimulationSettingEntity> settingOptional = settingRepository.findByUserIdAndStatus(userId, "IN_PROGRESS");

        if (settingOptional.isPresent()) {
            SimulationSettingEntity setting = settingOptional.get();
            return scenarioRepository.findById(setting.getScenarioId())
                    .map(scenario -> {
                        // 필요하다면 DTO에 더 많은 정보(예: 마지막 질문 번호)를 추가할 수 있습니다.
                        return Simulation.builder()
                                .scenarioId(scenario.getScenarioId())
                                .scenarioName(scenario.getScenarioName())
                                // ... 기타 필요한 정보
                                .build();
                    });
        }
        log.info("No IN_PROGRESS simulation found for userId: {}", userId);
        return Optional.empty();
    }

    // --- Helper Methods ---

    /**
     * ✅ [초기 상태] 사용자의 선택 이력을 기반으로 가장 지배적인 성향을 결정합니다.
     * 이 메서드는 다시 활성화됩니다.
     */
    private String determineDominantTrait(List<SimulationChoiceEntity> choices) {
        if (choices == null || choices.isEmpty()) {
            return "UNKNOWN";
        }
        return choices.stream()
                .map(SimulationChoiceEntity::getSelectedTrait) // trait 문자열만 추출
                .filter(Objects::nonNull) // null 값이 있다면 이 단계에서 걸러냅니다.
                .collect(Collectors.groupingBy(trait -> trait, Collectors.counting())) // null이 없는 안전한 상태에서 그룹화
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("UNKNOWN"); // 가장 많은 성향이 없거나, 모든 성향이 null/비어있으면 UNKNOWN 반환
    }

    /**
     * ✅ [초기 상태] SimulationQuestionEntity를 SimulationQuestion DTO로 변환합니다.
     * 이 메서드는 resultPersonalityType 필드를 파싱하지 않습니다.
     */
    private SimulationQuestion convertToQuestionDTO(SimulationQuestionEntity entity) {
        try {
            // 이 부분에서는 SimulationQuestion.JsonOptions 클래스가 trait 필드를 포함하고 있어야 합니다.
            SimulationQuestion.JsonOptions jsonOptions = objectMapper.readValue(entity.getChoiceOptions(), SimulationQuestion.JsonOptions.class);
            return SimulationQuestion.builder()
                    .scenarioId(entity.getScenarioId())
                    .questionNumber(entity.getQuestionNumber())
                    .questionText(entity.getQuestionText())
                    .options(jsonOptions.getOptions())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Error parsing choice options JSON for question ID {}: {}", entity.getQuestionId(), e.getMessage());
            throw new IllegalStateException("Failed to parse question options.", e);
        }
    }
}