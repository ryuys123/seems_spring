package com.test.seems.simulation.controller;

import com.test.seems.simulation.model.dto.*;
import com.test.seems.simulation.model.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    // 1. 활성화된 시나리오 목록 조회
    // GET /api/simulation/scenarios
    @GetMapping("/scenarios")
    public ResponseEntity<List<Simulation>> getScenarios() {
        List<Simulation> scenarios = simulationService.getActiveScenarios();
        return ResponseEntity.ok(scenarios);
    }

    // 2. 시뮬레이션 시작 및 첫 질문 생성
    // POST /api/simulation/start
    @PostMapping("/start")
    public ResponseEntity<SimulationQuestion> startSimulation(@RequestBody StartSimulationRequest request) {
        // 서비스에서 시뮬레이션 세션을 시작하고 첫 질문을 생성
        SimulationQuestion firstQuestion = simulationService.startSimulation(request.getScenarioId(), request.getUserId());

        if (firstQuestion != null) {
            // 첫 질문 정보를 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(firstQuestion);
        } else {
            // 시뮬레이션 시작 실패 (예: AI 연동 오류 등)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. 시뮬레이션 진행 (선택지 제출 및 다음 질문 받기)
    // POST /api/simulation/progress
    @PostMapping("/progress")
    public ResponseEntity<SimulationQuestion> progressSimulation(@RequestBody ProgressSimulationRequest request) {
        // 서비스에서 선택을 저장하고 AI를 통해 다음 질문 생성
        SimulationQuestion nextQuestion = simulationService.processChoiceAndGenerateNextQuestion(
                request.getSettingId(),
                request.getQuestionNumber(),
                request.getChoiceText(),
                request.getSelectedTrait()
        );

        if (nextQuestion != null) {
            // AI가 생성한 다음 질문 반환
            return ResponseEntity.ok(nextQuestion);
        } else {
            // 다음 질문 생성이 실패했거나 (AI 응답 오류 등),
            // 시뮬레이션이 종료 단계일 경우 (resultDTO를 반환해야 함)
            // 이 단계에서는 시뮬레이션 종료 시점을 별도로 처리해야 할 수 있습니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 4. 시뮬레이션 결과 완료 및 분석
    // POST /api/simulation/complete/{settingId}
    @PostMapping("/complete/{settingId}")
    public ResponseEntity<SimulationResultDTO> completeSimulation(@PathVariable Long settingId) {
        // 시뮬레이션 결과를 분석하고 저장
        SimulationResultDTO result = simulationService.analyzeAndSaveResult(settingId);

        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 5. 중간 저장된 시뮬레이션 불러오기
    // GET /api/simulation/resume?userId={userId}
    @GetMapping("/resume")
    public ResponseEntity<Simulation> resumeSimulation(@RequestParam String userId) {
        // 사용자의 가장 최근 세션 정보를 조회
        Optional<Simulation> setting = simulationService.resumeSimulation(userId);

        if (setting.isPresent()) {
            return ResponseEntity.ok(setting.get());
        } else {
            // 저장된 진행 상황이 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}