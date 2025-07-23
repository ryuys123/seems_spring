package com.test.seems.analysis.controller;

import com.test.seems.analysis.model.UserAnalysisSummaryDto;
import com.test.seems.analysis.model.UserTaskStatus;
import com.test.seems.analysis.service.UserTaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
public class UserTaskStatusController {

    @Autowired
    private UserTaskStatusService userTaskStatusService;

    // 현재 로그인한 사용자의 과제 완료 상태 조회
    @GetMapping("/status/{userId}")
    public ResponseEntity<UserTaskStatus> getUserTaskStatus(@PathVariable String userId) {
        UserTaskStatus status = userTaskStatusService.getUserTaskStatus(userId);
        return ResponseEntity.ok(status);
    }

    // 통합 분석 시작 (모든 과제 완료 시)
    @PostMapping("/integrated/{userId}")
    public ResponseEntity<UserTaskStatus> startIntegratedAnalysis(@PathVariable String userId) {
        String result = userTaskStatusService.performIntegratedAnalysis(userId);
        if (result.startsWith("모든 과제가 완료되지 않아")) {
            // 실패 시에도 현재 상태를 반환하여 클라이언트가 업데이트된 상태를 받을 수 있도록 함
            UserTaskStatus status = userTaskStatusService.getUserTaskStatus(userId);
            return ResponseEntity.badRequest().body(status);
        }
        // 성공 시 업데이트된 상태를 반환
        UserTaskStatus updatedStatus = userTaskStatusService.getUserTaskStatus(userId);
        return ResponseEntity.ok(updatedStatus);
    }

    // 최종 분석 결과 조회
    @GetMapping("/final-result/{userId}")
    public ResponseEntity<UserAnalysisSummaryDto> getFinalAnalysisResult(@PathVariable String userId) {
        UserAnalysisSummaryDto result = userTaskStatusService.getFinalAnalysisResult(userId);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }
}