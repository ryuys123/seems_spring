package com.test.seems.simulation.controller;

import com.test.seems.simulation.model.dto.*;
import com.test.seems.simulation.model.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * React 프론트엔드와 통신하여 심리 시뮬레이션의 전체 흐름을 관리하는 컨트롤러입니다.
 * 개발 환경에서는 localhost:3000에서의 요청을 허용합니다.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    // ✨ '극복 시뮬레이션 시작'을 위한 API
    @PostMapping("/start/coping")
    public ResponseEntity<SimulationQuestion> startCopingSimulation(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        SimulationQuestion firstStep = simulationService.startCopingSimulation(userId);
        return ResponseEntity.ok(firstStep);
    }

    // ✨ 2. '극복 시뮬레이션'의 다음 단계를 위한 API 추가
    @PostMapping("/continue/coping")
    public ResponseEntity<Map<String, Object>> continueCopingSimulation(@RequestBody Map<String, Object> payload) {
        List<Map<String, Object>> history = (List<Map<String, Object>>) payload.get("history");
        String choiceText = (String) payload.get("choiceText");

        Map<String, Object> nextStep = simulationService.continueCopingSimulation(history, choiceText);
        return ResponseEntity.ok(nextStep);
    }

    /**
     * ✅ [수정됨] GET /api/simulation/list
     * 활성화된 모든 시뮬레이션 목록을 조회합니다.
     * @return 활성화된 시뮬레이션 DTO 목록 (List<Simulation>)
     */
    @GetMapping("/list") // 프론트엔드의 '/list' 요청에 응답
    public ResponseEntity<List<Simulation>> getActiveSimulationsList() { // 메서드 이름도 명확하게 변경
        // 서비스의 getActiveSimulations() 메서드를 호출하여 목록을 가져옵니다.
        List<Simulation> activeSimulations = simulationService.getActiveSimulations();
        return ResponseEntity.ok(activeSimulations); // 목록을 반환
    }

    /**
     * ✅ [신규] GET /api/simulation/today
     * 프론트엔드 메인 페이지에 보여줄 '오늘의 시뮬레이션' 정보를 조회합니다.
     * @return 오늘 요일에 해당하는 시나리오 DTO. 만약 오늘 해당하는 시나리오가 없다면 404 Not Found를 반환합니다.
     */
    @GetMapping("/today") // '오늘의 시뮬레이션'을 위한 새로운 엔드포인트
    public ResponseEntity<Simulation> getTodaysSimulation() {
        Optional<Simulation> todaySimulation = simulationService.getTodaysSimulation();
        return todaySimulation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * [POST /api/simulation/start]
     * 사용자가 특정 시나리오를 선택하고 '시작하기'를 눌렀을 때 호출됩니다.
     * 첫 번째 질문 데이터를 반환합니다. (변경 없음)
     * @param request scenarioId, userId를 담은 요청 DTO
     * @return 생성된 첫 번째 질문 DTO
     */
    @PostMapping("/start")
    public ResponseEntity<SimulationQuestion> startSimulation(@RequestBody StartSimulationRequest request) {
        SimulationQuestion firstQuestion = simulationService.startSimulation(request.getScenarioId(), request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(firstQuestion);
    }

    /**
     * [POST /api/simulation/progress]
     * 사용자가 시뮬레이션 진행 중 선택지를 고를 때마다 호출됩니다.
     * 다음 질문 또는 시뮬레이션 종료 신호를 반환합니다. (변경 없음)
     * @param request settingId, 질문 번호, 선택 내용 등을 담은 요청 DTO
     * @return 다음 질문 DTO (종료 신호 포함)
     */
    @PostMapping("/progress")
    public ResponseEntity<SimulationQuestion> progressSimulation(@RequestBody ProgressSimulationRequest request) {
        SimulationQuestion nextStep = simulationService.processChoiceAndGenerateNextQuestion(
                request.getSettingId(),
                request.getQuestionNumber(),
                request.getChoiceText(),
                request.getSelectedTrait()
        );
        return ResponseEntity.ok(nextStep);
    }

    /**
     * [POST /api/simulation/complete/{settingId}]
     * /progress API가 종료 신호(isSimulationEnded: true)를 보내면,
     * 프론트엔드에서 이 API를 호출하여 최종 분석 결과를 요청합니다. (변경 없음)
     * @param settingId 현재 진행 중인 시뮬레이션의 세션 ID
     * @return 분석된 최종 결과 DTO
     */
    @PostMapping("/complete/{settingId}")
    public ResponseEntity<SimulationResult> completeSimulation(@PathVariable Long settingId) {
        // 이 메서드는 SimulationService.analyzeAndSaveResult(settingId)를 호출합니다.
        // 현재 서비스 로직이 단일 인자(settingId)만 받는 것으로 복원되었음을 가정합니다.
        SimulationResult result = simulationService.analyzeAndSaveResult(settingId);
        return ResponseEntity.ok(result);
    }

    /**
     * [GET /api/simulation/resume]
     * 사용자가 중간에 이탈했다가 다시 접속했을 때,
     * 가장 최근에 진행 중이던 시뮬레이션을 이어하기 위해 호출합니다. (변경 없음)
     * @param userId 현재 접속한 사용자의 ID
     * @return 진행 중이던 시뮬레이션 세션 정보 DTO
     */
    @GetMapping("/resume")
    public ResponseEntity<Simulation> resumeSimulation(@RequestParam String userId) {
        Optional<Simulation> setting = simulationService.resumeSimulation(userId);
        return setting.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}