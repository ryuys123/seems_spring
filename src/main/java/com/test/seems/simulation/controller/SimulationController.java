package com.test.seems.simulation.controller;

import com.test.seems.simulation.model.dto.SimulationQuestion; // SimulationQuestion DTO는 유지
import com.test.seems.simulation.model.dto.SimulationResult; // SimulationResult DTO는 유지
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
 * 이제 모든 시뮬레이션은 AI 기반의 '극복 시뮬레이션'으로 통합됩니다.
 * 개발 환경에서는 localhost:3000에서의 요청을 허용합니다.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/simulation") // 기본 경로 유지
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    /**
     * POST /api/simulation/start
     * 맞춤형 극복 시뮬레이션을 시작하는 API입니다.
     * @param payload userId를 담은 요청 본문 (예: {"userId": "testUser"})
     * @return 첫 번째 질문 정보 DTO
     */
    @PostMapping("/start")
    public ResponseEntity<SimulationQuestion> startSimulation(@RequestBody Map<String, String> payload) {
        try {
            String userId = payload.get("userId");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            SimulationQuestion firstStep = simulationService.startSimulation(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(firstStep);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * POST /api/simulation/continue
     * 진행 중인 시뮬레이션의 다음 단계를 요청하는 API입니다.
     * @param payload settingId, history, choiceText를 담은 요청 본문
     * @return 다음 질문 정보 또는 시뮬레이션 종료 시 최종 결과
     */
    @PostMapping("/continue")
    public ResponseEntity<Map<String, Object>> continueSimulation(@RequestBody Map<String, Object> payload) {
        try {
            Long settingId = ((Number) payload.get("settingId")).longValue();
            List<Map<String, Object>> history = (List<Map<String, Object>>) payload.get("history");
            String choiceText = (String) payload.get("choiceText");

            Map<String, Object> nextStep = simulationService.continueSimulation(settingId, history, choiceText);
            return ResponseEntity.ok(nextStep);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "시뮬레이션 진행 중 오류가 발생했습니다.", "detail", e.getMessage()));
        }
    }

    /**
     * POST /api/simulation/end
     * 시뮬레이션의 최종 분석을 요청하고 결과를 저장하는 API입니다.
     * @param payload settingId, history를 담은 요청 본문
     * @return 최종 분석 결과 DTO
     */
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endSimulation(@RequestBody Map<String, Object> payload) {
        try {
            Long settingId = ((Number) payload.get("settingId")).longValue();
            List<Map<String, Object>> history = (List<Map<String, Object>>) payload.get("history");

            SimulationResult finalResult = simulationService.analyzeAndSaveResult(settingId, history);

            return ResponseEntity.ok(Map.of("isSimulationEnded", true, "result", finalResult));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "시뮬레이션 결과 처리 중 오류가 발생했습니다.", "detail", e.getMessage()));
        }
    }

    /**
     * GET /api/simulation/latest-result/{userId}
     * 사용자의 가장 최근 완료된 시뮬레이션 결과를 요약 조회합니다.
     * @param userId 사용자 ID
     * @return SimulationResult DTO 또는 404 Not Found
     */
    @GetMapping("/latest-result/{userId}")
    public ResponseEntity<SimulationResult> getLatestSimulationResult(@PathVariable String userId) {
        Optional<SimulationResult> result = simulationService.getLatestSimulationResult(userId);
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/simulation/resume
     * 사용자가 중간에 이탈했다가 다시 접속했을 때,
     * 가장 최근에 진행 중이던 시뮬레이션을 이어하기 위해 호출합니다.
     * 이제 모든 시뮬레이션은 극복 시뮬레이션이므로 시나리오 ID는 무시됩니다.
     *
     * @param userId 현재 접속한 사용자의 ID
     * @return 진행 중이던 시뮬레이션 세션 정보 DTO (Map 형태)
     */
    @GetMapping("/resume")
    // ✅ 반환 타입을 ResponseEntity<Map<String, Object>>로 변경합니다.
    public ResponseEntity<Map<String, Object>> resumeSimulation(@RequestParam String userId) {
        // service.resumeSimulation은 Optional<Map<String, Object>>를 반환하도록 변경될 예정입니다.
        Optional<Map<String, Object>> setting = simulationService.resumeSimulation(userId);
        return setting.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}