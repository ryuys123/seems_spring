package com.test.seems.quest.controller;

import com.test.seems.quest.model.dto.QuestRecommendationDto;
import com.test.seems.quest.model.service.QuestRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quest-recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QuestRecommendationController {
    private final QuestRecommendationService questRecommendationService;

    @GetMapping(params = "emotionId")
    public ResponseEntity<List<QuestRecommendationDto>> getRecommendations(@RequestParam Long emotionId) {
        List<QuestRecommendationDto> recommendations = questRecommendationService.getRecommendationsByEmotion(emotionId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping(params = "userId")
    public ResponseEntity<List<QuestRecommendationDto>> getRecommendationsByUserId(@RequestParam String userId) {
        List<QuestRecommendationDto> recommendations = questRecommendationService.getRecommendationsByUserId(userId);
        return ResponseEntity.ok(recommendations);
    }
} 