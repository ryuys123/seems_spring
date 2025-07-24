package com.test.seems.quest.controller;

import com.test.seems.quest.exception.QuestException;
import com.test.seems.quest.model.dto.PurchaseRequestDto;
import com.test.seems.quest.model.dto.QuestRewardDto;
import com.test.seems.quest.model.dto.UserPointsDto;
import com.test.seems.quest.model.dto.UserStatsDto;
import com.test.seems.quest.model.service.QuestRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class QuestRewardController {
    private final QuestRewardService questRewardService;
    /**
     * 모든 뱃지 보상 목록 조회
     */
    @GetMapping("/quest-rewards")
    public ResponseEntity<List<QuestRewardDto>> getAllQuestRewards() {
        try {
            List<QuestRewardDto> rewards = questRewardService.getAllQuestRewards();
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            log.error("Failed to get quest rewards", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 사용자 포인트 조회
     */
    @GetMapping("/user/points")
    public ResponseEntity<UserPointsDto> getUserPoints(@RequestParam String userId) {
        try {
            UserPointsDto userPoints = questRewardService.getUserPoints(userId);
            return ResponseEntity.ok(userPoints);
        } catch (Exception e) {
            log.error("Failed to get user points for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 사용자 통계 조회
     */
    @GetMapping("/user/stats")
    public ResponseEntity<UserStatsDto> getUserStats(@RequestParam String userId) {
        try {
            UserStatsDto userStats = questRewardService.getUserStats(userId);
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            log.error("Failed to get user stats for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * (변경) 내 뱃지 목록 조회 - rewardId, isEquipped 포함
     */
    @GetMapping("/user/owned-titles")
    public ResponseEntity<List<Map<String, Object>>> getOwnedRewardIdsWithEquipped(@RequestParam String userId) {
        try {
            List<Map<String, Object>> ownedBadges = questRewardService.getOwnedRewardIdsWithEquipped(userId);
            return ResponseEntity.ok(ownedBadges);
        } catch (Exception e) {
            log.error("Failed to get owned reward ids for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 뱃지 장착 API
     */
    @PostMapping("/user/equip-badge")
    public ResponseEntity<?> equipBadge(@RequestParam String userId, @RequestBody Map<String, Object> request) {
        try {
            Long rewardId = Long.valueOf(request.get("rewardId").toString());
            questRewardService.equipBadge(userId, rewardId);
            // 장착 후 최신 ownedItems 목록 반환
            List<Map<String, Object>> ownedBadges = questRewardService.getOwnedRewardIdsWithEquipped(userId);
            return ResponseEntity.ok(ownedBadges);
        } catch (Exception e) {
            log.error("Failed to equip badge for userId: {}, rewardId: {}", userId, request.get("rewardId"), e);
            return ResponseEntity.internalServerError().body("뱃지 장착 중 오류가 발생했습니다.");
        }
    }
    /**
     * (변경) 뱃지 구매 - isEquipped 포함 응답
     */
    @PostMapping("/quest-rewards/purchase")
    public ResponseEntity<Map<String, Object>> purchaseReward(@RequestParam String userId, @RequestBody Map<String, Object> request) {
        try {
            Long rewardId = Long.valueOf(request.get("rewardId").toString());
            Map<String, Object> result = questRewardService.purchaseRewardWithEquipped(userId, rewardId);
            return ResponseEntity.ok(result);
        } catch (QuestException e) {
            log.warn("Purchase failed for userId: {}, rewardId: {}, reason: {}", userId, request.get("rewardId"), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to purchase reward for userId: {}, rewardId: {}", userId, request.get("rewardId"), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "구매 중 오류가 발생했습니다."));
        }
    }
    /**
     * 퀘스트 상점 메인 페이지 데이터 조회
     */
    @GetMapping("/quest-store/{userId}")
    public ResponseEntity<QuestRewardService.QuestStoreDto> getQuestStoreData(@PathVariable String userId) {
        try {
            QuestRewardService.QuestStoreDto storeData = questRewardService.getQuestStoreData(userId);
            return ResponseEntity.ok(storeData);
        } catch (Exception e) {
            log.error("Failed to get quest store data for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 포인트 추가
     */
    @PostMapping("/user/points/add")
    public ResponseEntity<String> addPoints(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Integer points = Integer.valueOf(request.get("points").toString());
            questRewardService.addPoints(userId, points);
            return ResponseEntity.ok("포인트가 추가되었습니다.");
        } catch (QuestException e) {
            log.warn("Points addition failed for userId: {}, points: {}, reason: {}", 
                    request.get("userId"), request.get("points"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to add points for userId: {}, points: {}", 
                    request.get("userId"), request.get("points"), e);
            return ResponseEntity.internalServerError().body("포인트 추가 중 오류가 발생했습니다.");
        }
    }
    /**
     * 포인트 차감
     */
    @PostMapping("/user/points/deduct")
    public ResponseEntity<String> deductPoints(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Integer points = Integer.valueOf(request.get("points").toString());
            questRewardService.deductPoints(userId, points);
            return ResponseEntity.ok("포인트가 차감되었습니다.");
        } catch (QuestException e) {
            log.warn("Points deduction failed for userId: {}, points: {}, reason: {}", 
                    request.get("userId"), request.get("points"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to deduct points for userId: {}, points: {}", 
                    request.get("userId"), request.get("points"), e);
            return ResponseEntity.internalServerError().body("포인트 차감 중 오류가 발생했습니다.");
        }
    }
    /**
     * 포인트 업데이트 (통합)
     */
    @PutMapping("/user/points")
    public ResponseEntity<String> updatePoints(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Integer points = Integer.valueOf(request.get("points").toString());
            questRewardService.updatePoints(userId, points);
            return ResponseEntity.ok("포인트가 업데이트되었습니다.");
        } catch (QuestException e) {
            log.warn("Points update failed for userId: {}, points: {}, reason: {}", 
                    request.get("userId"), request.get("points"), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update points for userId: {}, points: {}", 
                    request.get("userId"), request.get("points"), e);
            return ResponseEntity.internalServerError().body("포인트 업데이트 중 오류가 발생했습니다.");
        }
    }
    /**
     * 장착중인 뱃지 1개 + 상세정보 반환
     */
    @GetMapping("/user/equipped-badge")
    public ResponseEntity<Map<String, Object>> getEquippedBadge(@RequestParam String userId) {
        Map<String, Object> badge = questRewardService.getEquippedBadge(userId);
        if (badge == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(badge);
    }
} 