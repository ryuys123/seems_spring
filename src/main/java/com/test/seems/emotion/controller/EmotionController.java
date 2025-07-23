package com.test.seems.emotion.controller;

import com.test.seems.emotion.jpa.entity.Emotion;
import com.test.seems.emotion.jpa.entity.EmotionLog;
import com.test.seems.emotion.model.service.EmotionService;
import com.test.seems.emotion.model.dto.TodayEmotionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class EmotionController {

    @Autowired
    private EmotionService emotionService;

    @GetMapping("/emotions")
    public ResponseEntity<List<Emotion>> getAllEmotions() {
        List<Emotion> emotions = emotionService.getAllEmotions();
        return ResponseEntity.ok(emotions);
    }

    @PostMapping("/emotion-logs")
    public ResponseEntity<EmotionLog> createEmotionLog(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Long emotionId = ((Number) payload.get("emotionId")).longValue();
        String textContent = (String) payload.get("textContent");

        if (userId == null || emotionId == null || textContent == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            EmotionLog savedLog = emotionService.saveEmotionLog(userId, emotionId, textContent);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLog);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/emotion-logs/{userId}")
    public ResponseEntity<List<EmotionLog>> getUserEmotionLogs(@PathVariable String userId) {
        List<EmotionLog> emotionLogs = emotionService.getUserEmotionLogs(userId);
        return ResponseEntity.ok(emotionLogs);
    }
    
    @GetMapping("/today-emotion")
    public ResponseEntity<TodayEmotionDto> getTodayEmotion(@RequestParam String userId) {
        TodayEmotionDto todayEmotion = emotionService.getTodayEmotion(userId);
        if (todayEmotion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todayEmotion);
    }
}