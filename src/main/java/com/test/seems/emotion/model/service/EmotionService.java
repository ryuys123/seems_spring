package com.test.seems.emotion.model.service;

import com.test.seems.emotion.jpa.entity.Emotion;
import com.test.seems.emotion.jpa.entity.EmotionLog;
import com.test.seems.emotion.jpa.repository.EmotionRepository;
import com.test.seems.emotion.jpa.repository.EmotionLogRepository;
import com.test.seems.emotion.model.dto.TodayEmotionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

@Service
public class EmotionService {

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private EmotionLogRepository emotionLogRepository;

    private static final Map<String, String> EMOTION_EMOJIS = new HashMap<>();
    static {
        EMOTION_EMOJIS.put("행복", "😊");
        EMOTION_EMOJIS.put("슬픔", "😔");
        EMOTION_EMOJIS.put("화남", "😡");
        EMOTION_EMOJIS.put("평온", "😌");
        EMOTION_EMOJIS.put("불안", "😰");
        EMOTION_EMOJIS.put("피곤", "😴");
        EMOTION_EMOJIS.put("고민", "🤔");
        EMOTION_EMOJIS.put("자신감", "😎");
    }

    public List<Emotion> getAllEmotions() {
        List<Emotion> emotions = emotionRepository.findAll();
        emotions.forEach(emotion -> emotion.setEmoji(EMOTION_EMOJIS.getOrDefault(emotion.getEmotionName(), "❓")));
        return emotions;
    }

    public EmotionLog saveEmotionLog(String userId, Long emotionId, String textContent) {
        Optional<Emotion> emotionOptional = emotionRepository.findById(emotionId);
        if (emotionOptional.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 감정 ID입니다: " + emotionId);
        }

        EmotionLog emotionLog = new EmotionLog();
        emotionLog.setUserId(userId);
        emotionLog.setEmotion(emotionOptional.get());
        emotionLog.setTextContent(textContent);
        return emotionLogRepository.save(emotionLog);
    }

    public List<EmotionLog> getUserEmotionLogs(String userId) {
        List<EmotionLog> logs = emotionLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // 각 로그의 감정 객체에 이모지 설정
        logs.forEach(log -> {
            if (log.getEmotion() != null) {
                log.getEmotion().setEmoji(EMOTION_EMOJIS.getOrDefault(log.getEmotion().getEmotionName(), "❓"));
            }
        });
        return logs;
    }
    
    public TodayEmotionDto getTodayEmotion(String userId) {
        // 먼저 오늘의 감정을 조회
        Optional<EmotionLog> todayEmotion = emotionLogRepository.findTodayEmotionByUserId(userId);
        
        // 오늘의 감정이 없으면 가장 최근 감정을 조회
        if (todayEmotion.isEmpty()) {
            todayEmotion = emotionLogRepository.findLatestEmotionByUserId(userId);
        }
        
        if (todayEmotion.isEmpty()) {
            return null; // 감정 기록이 없는 경우
        }
        
        EmotionLog emotionLog = todayEmotion.get();
        Emotion emotion = emotionLog.getEmotion();
        
        // 날짜 포맷팅
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedDate = sdf.format(emotionLog.getCreatedAt());
        
        return TodayEmotionDto.builder()
                .emotionId(emotion.getEmotionId())
                .emotionName(emotion.getEmotionName())
                .emoji(EMOTION_EMOJIS.getOrDefault(emotion.getEmotionName(), "❓"))
                .description(emotion.getDescription())
                .textContent(emotionLog.getTextContent())
                .createdAt(formattedDate)
                .build();
    }
}