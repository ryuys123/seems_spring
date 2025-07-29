package com.test.seems.user.controller;

import com.test.seems.user.model.dto.ActivityDto;
import com.test.seems.user.model.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class UserActivityController {
    
    private final UserActivityService userActivityService;
    
    /**
     * 활동 기록 저장
     */
    @PostMapping("/activities")
    public ResponseEntity<Map<String, Object>> recordActivity(@RequestBody Map<String, Object> request) {
        try {
            log.info("=== 활동 기록 API 호출됨 ===");
            log.info("요청 URL: /api/user/activities");
            log.info("요청 본문: {}", request);
            
            String userId = (String) request.get("userId");
            String activityType = (String) request.get("activityType");
            String activityDescription = (String) request.get("activityDescription");
            
            log.info("활동 기록 요청: userId={}, type={}, description={}", 
                    userId, activityType, activityDescription);
            
            // 활동 기록 저장 로직 (임시)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "활동이 기록되었습니다.");
            
            log.info("활동 기록 성공: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("활동 기록 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "활동 기록 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사용자 최근활동 조회
     */
    @GetMapping("/activities")
    public ResponseEntity<Map<String, Object>> getRecentActivity(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<ActivityDto> activities = userActivityService.getRecentActivities(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("activities", activities);
            response.put("totalCount", activities.size());
            
            log.info("최근활동 조회 성공: userId={}, count={}", userId, activities.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("최근활동 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "최근활동 조회 중 오류가 발생했습니다."));
        }
    }
} 