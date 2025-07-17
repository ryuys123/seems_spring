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
     * 사용자가 보유한 뱃지 ID 목록 조회
     */
    @GetMapping("/user/owned-titles")
    public ResponseEntity<List<Long>> getOwnedRewardIds(@RequestParam String userId) {
        try {
            List<Long> ownedRewardIds = questRewardService.getOwnedRewardIds(userId);
            return ResponseEntity.ok(ownedRewardIds);
        } catch (Exception e) {
            log.error("Failed to get owned reward ids for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 뱃지 구매
     */
    @PostMapping("/quest-rewards/purchase")
    public ResponseEntity<String> purchaseReward(@RequestParam String userId, @RequestBody PurchaseRequestDto request) {
        try {
            questRewardService.purchaseReward(userId, request);
            return ResponseEntity.ok("뱃지 구매가 완료되었습니다.");
        } catch (QuestException e) {
            log.warn("Purchase failed for userId: {}, rewardId: {}, reason: {}", userId, request.getRewardId(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to purchase reward for userId: {}, rewardId: {}", userId, request.getRewardId(), e);
            return ResponseEntity.internalServerError().body("구매 중 오류가 발생했습니다.");
        }
    }
} 