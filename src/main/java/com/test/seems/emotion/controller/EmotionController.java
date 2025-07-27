package com.test.seems.emotion.controller;

import com.test.seems.emotion.jpa.entity.Emotion;
import com.test.seems.emotion.jpa.entity.EmotionLog;
import com.test.seems.emotion.model.service.EmotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

    @GetMapping("/emotion-logs/{userId}/today")
    public ResponseEntity<Map<String, Object>> getTodayEmotionLog(@PathVariable String userId) {
        log.info("오늘의 감정 로그 조회 요청: userId={}", userId);
        
        EmotionLog todayLog = emotionService.getTodayLatestEmotionLog(userId);
        if (todayLog == null) {
            log.warn("오늘의 감정 로그가 없음: userId={}", userId);
            return ResponseEntity.noContent().build();
        }
        
        log.info("감정 로그 조회 성공: emotionLogId={}, emotion={}", 
                todayLog.getEmotionLogId(), 
                todayLog.getEmotion() != null ? todayLog.getEmotion().getEmotionName() : "null");
        
        // 프론트엔드가 기대하는 구조로 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", todayLog.getTextContent());
        response.put("createdAt", todayLog.getCreatedAt());
        
        if (todayLog.getEmotion() != null) {
            Map<String, Object> emotion = new HashMap<>();
            emotion.put("emotionId", todayLog.getEmotion().getEmotionId());
            emotion.put("emotionName", todayLog.getEmotion().getEmotionName());
            emotion.put("description", todayLog.getEmotion().getDescription());
            emotion.put("emoji", todayLog.getEmotion().getEmoji());
            response.put("emotion", emotion);
            log.info("감정 정보 설정 완료: emotionName={}, emoji={}", 
                    todayLog.getEmotion().getEmotionName(), todayLog.getEmotion().getEmoji());
        } else {
            // emotion이 null인 경우 빈 객체라도 보내기
            response.put("emotion", new HashMap<>());
            log.warn("감정 정보가 null이므로 빈 객체로 설정");
        }
        
        log.info("최종 응답: {}", response);
        return ResponseEntity.ok(response);
    }
}