package com.test.seems.quest.controller;

import com.test.seems.quest.exception.QuestException;
import com.test.seems.quest.model.dto.QuestDto;
import com.test.seems.quest.model.service.QuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class QuestController {
    
    private final QuestService questService;
    
    /**
     * 사용자의 모든 퀘스트 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuestDto>> getUserQuests(@PathVariable String userId) {
        try {
            List<QuestDto> quests = questService.getUserQuests(userId);
            return ResponseEntity.ok(quests);
        } catch (Exception e) {
            log.error("Failed to get user quests for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 사용자의 완료된 퀘스트 조회
     */
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<QuestDto>> getCompletedQuests(@PathVariable String userId) {
        try {
            List<QuestDto> quests = questService.getCompletedQuests(userId);
            return ResponseEntity.ok(quests);
        } catch (Exception e) {
            log.error("Failed to get completed quests for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 사용자의 미완료 퀘스트 조회
     */
    @GetMapping("/user/{userId}/incomplete")
    public ResponseEntity<List<QuestDto>> getIncompleteQuests(@PathVariable String userId) {
        try {
            List<QuestDto> quests = questService.getIncompleteQuests(userId);
            return ResponseEntity.ok(quests);
        } catch (Exception e) {
            log.error("Failed to get incomplete quests for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 퀘스트 완료 처리
     */
    @PostMapping("/{questId}/complete")
    public ResponseEntity<String> completeQuest(@PathVariable Long questId, @RequestParam String userId) {
        try {
            questService.completeQuest(userId, questId);
            return ResponseEntity.ok("퀘스트가 완료되었습니다.");
        } catch (QuestException e) {
            log.warn("Quest completion failed for userId: {}, questId: {}, reason: {}", userId, questId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to complete quest for userId: {}, questId: {}", userId, questId, e);
            return ResponseEntity.internalServerError().body("퀘스트 완료 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 퀘스트 단계 완료 처리
     */
    @PostMapping("/{questId}/steps/{stepId}/complete")
    public ResponseEntity<String> completeQuestStep(
            @PathVariable Long questId, 
            @PathVariable String stepId, 
            @RequestParam String userId,
            @RequestParam(defaultValue = "true") Boolean completed) {
        try {
            questService.completeQuestStep(userId, questId, stepId, completed);
            return ResponseEntity.ok("퀘스트 단계가 완료되었습니다.");
        } catch (QuestException e) {
            log.warn("Quest step completion failed for userId: {}, questId: {}, stepId: {}, reason: {}", 
                    userId, questId, stepId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to complete quest step for userId: {}, questId: {}, stepId: {}", 
                    userId, questId, stepId, e);
            return ResponseEntity.internalServerError().body("퀘스트 단계 완료 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 새로운 퀘스트 생성
     */
    @PostMapping("/create")
    public ResponseEntity<QuestDto> createQuest(@RequestParam String userId, 
                                               @RequestParam String questName, 
                                               @RequestParam(defaultValue = "5") Integer questPoints) {
        try {
            QuestDto quest = questService.createQuest(userId, questName, questPoints);
            return ResponseEntity.ok(quest);
        } catch (Exception e) {
            log.error("Failed to create quest for userId: {}, questName: {}", userId, questName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 사용자의 퀘스트 통계 조회
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<QuestService.QuestStatsDto> getQuestStats(@PathVariable String userId) {
        try {
            QuestService.QuestStatsDto stats = questService.getQuestStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get quest stats for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 