package com.test.seems.simulation.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import com.test.seems.analysis.jpa.repository.UserAnalysisSummaryRepository;
import com.test.seems.simulation.jpa.entity.*;
import com.test.seems.simulation.jpa.repository.*;
import com.test.seems.simulation.model.dto.Simulation;
import com.test.seems.simulation.model.dto.SimulationQuestion;
import com.test.seems.simulation.model.dto.SimulationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    // ✨ 1. 새로운 의존성 추가
    private final UserAnalysisSummaryRepository userAnalysisSummaryRepository;
    private final AiGenerationService aiGenerationService;

    // --- 의존성 주입 (Repositories & ObjectMapper) ---
    private final ScenarioRepository scenarioRepository;
    private final SimulationSettingRepository settingRepository;
    private final SimulationQuestionRepository questionRepository;
    private final SimulationChoiceRepository choiceRepository;
    private final SimulationResultRepository resultRepository; // 템플릿용
    private final SimulationUserResultRepository userResultRepository; // 사용자 결과 저장용
    private final ObjectMapper objectMapper;

    // ✨ 1. '극복 시뮬레이션'의 다음 단계를 위한 메서드 추가
    public Map<String, Object> continueCopingSimulation(List<Map<String, Object>> history, String choiceText) {
        // Python AI 서버에 보낼 프롬프트를 생성합니다.
        String prompt = createContinuationPrompt(history, choiceText);

        // AiGenerationService를 통해 AI에게 다음 단계를 생성하도록 요청합니다.
        return aiGenerationService.generateCustomSimulationContinuation(prompt); // (AiGenerationService에 이 메서드 추가 필요)
    }

    // 다음 단계를 위한 프롬프트를 만드는 헬퍼 메서드
    private String createContinuationPrompt(List<Map<String, Object>> history, String choiceText) {
        // history와 choiceText를 문자열로 변환하여 프롬프트를 구성
        return String.format(
                "[역할 부여] 당신은 심리 시뮬레이션 AI입니다.\n" +
                        "[지금까지의 대화 내용]\n%s\n" +
                        "[사용자의 최근 선택]\n\"%s\"\n" +
                        "[시뮬레이션 목표] 사용자의 선택에 적절히 반응하고, 원래 목표에 맞춰 다음 단계의 시나리오를 이어서 생성해주세요.\n" +
                        "[JSON 출력 형식] (이전과 동일한 JSON 형식...)",
                history.toString(), choiceText
        );
    }

    // --- ✨ 2. '극복 시뮬레이션' 시작을 위한 새로운 메서드 추가 ---
    @Transactional
    public SimulationQuestion startCopingSimulation(String userId) {
        log.info("Starting coping simulation for userId: {}", userId);

        // 1. DB에서 사용자의 최신 종합 분석 결과를 조회합니다.
        UserAnalysisSummaryEntity summary = userAnalysisSummaryRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("종합 분석 결과가 없는 사용자입니다: " + userId));

        // 2. AiGenerationService를 통해 Python AI 서버에 맞춤형 시나리오 생성을 요청합니다.
        Map<String, Object> aiGeneratedStep = aiGenerationService.generateCustomSimulation(summary);

        // 3. AI가 생성한 시나리오를 위한 새로운 진행 정보(Setting)를 생성하고 저장합니다.
        //    이때 scenarioId는 없으므로 null로 두거나, '맞춤형'을 의미하는 특별한 ID(예: -1L)를 사용할 수 있습니다.
        SimulationSettingEntity setting = SimulationSettingEntity.builder()
                .userId(userId)
//                .scenarioId(-1L) // -1은 '맞춤형(AI 생성) 시나리오'를 의미한다고 가정
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .build();
        setting = settingRepository.save(setting);
        log.info("New COPING simulation setting created with ID: {}", setting.getSettingId());

        // 4. AI 응답(Map)을 프론트엔드에 전달할 DTO(SimulationQuestion)로 변환합니다.
        SimulationQuestion firstStepDto = mapAiResponseToDto(aiGeneratedStep);
        firstStepDto.setSettingId(setting.getSettingId()); // 진행 ID를 DTO에 포함
        firstStepDto.setIsSimulationEnded(false);

        return firstStepDto;
    }

    // ✨ 3. AI가 생성한 Map 데이터를 SimulationQuestion DTO로 변환하는 헬퍼 메서드
    private SimulationQuestion mapAiResponseToDto(Map<String, Object> aiResponse) {
        List<Map<String, String>> optionsFromAi = (List<Map<String, String>>) aiResponse.get("options");

        List<SimulationQuestion.ChoiceOption> options = optionsFromAi.stream()
                .map(optMap -> SimulationQuestion.ChoiceOption.builder()
                        .text(optMap.get("text"))
                        // nextNarrative 등 다른 필드도 필요 시 추가
                        .build())
                .collect(Collectors.toList());

        return SimulationQuestion.builder()
                .questionText((String) aiResponse.get("narrative"))
                .options(options)
                .build();
    }

    // ✨ '극복 시뮬레이션'을 종료하고 최종 결과를 분석/저장하는 메서드
    @Transactional
    public SimulationResult analyzeAndSaveCopingResult(Long settingId, List<Map<String, Object>> history) {

        // 1. Python AI 서버에 전체 대화 기록을 보내 최종 분석을 요청합니다.
        // (AiGenerationService에 endCopingSimulation 메서드가 필요합니다)
        Map<String, Object> finalAnalysis = aiGenerationService.endCopingSimulation(history);

        String resultSummary = (String) finalAnalysis.get("resultSummary");
        String personalityType = (String) finalAnalysis.get("personalityType"); // 예: '용감한 리더'
        String resultTitle = (String) finalAnalysis.get("finalSuggestion"); // 최종 제안을 제목으로 활용

        // 2. 받은 분석 결과를 SimulationUserResultEntity에 담아 DB에 저장합니다.
        SimulationUserResultEntity userResultEntity = SimulationUserResultEntity.builder()
                .settingId(settingId)
                .personalityType(personalityType)
                .resultTitle(resultTitle)
                .resultSummary(resultSummary)
                .createdAt(LocalDateTime.now())
                .build();

        SimulationUserResultEntity savedResult = userResultRepository.save(userResultEntity);

        // 3. 상태를 'COMPLETED'로 변경합니다.
        settingRepository.findById(settingId).ifPresent(setting -> {
            setting.setStatus("COMPLETED");
            settingRepository.save(setting);
        });

        return savedResult.toDto();
    }

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

    // ✨ '극복 시뮬레이션' 최종 분석 및 저장을 위한 API
    @PostMapping("/end/coping")
    public ResponseEntity<SimulationResult> endCopingSimulation(@RequestBody Map<String, Object> payload) {
        Long settingId = ((Number) payload.get("settingId")).longValue();
        List<Map<String, Object>> history = (List<Map<String, Object>>) payload.get("history");

        // ✅ 올바른 코드 (메서드 이름으로 바로 호출)
        SimulationResult finalResult = analyzeAndSaveCopingResult(settingId, history);
        return ResponseEntity.ok(finalResult);
    }
}