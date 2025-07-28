package com.test.seems.content.model.service;

import com.test.seems.content.jpa.entity.ContentEntity;
import com.test.seems.content.jpa.entity.EmotionContentRecommEntity;
import com.test.seems.content.jpa.repository.ContentRepository;
import com.test.seems.content.jpa.repository.EmotionContentRecommRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Service
public class ContentService {
    @Autowired
    private EmotionContentRecommRepository emotionContentRecommRepository;
    @Autowired
    private ContentRepository contentRepository;

    // 감정ID로 추천 유튜브 컨텐츠 리스트 반환
    public List<ContentEntity> getRecommendedContentsByEmotionId(Long emotionId) {
        List<EmotionContentRecommEntity> mappings = emotionContentRecommRepository.findByEmotionIdOrderByPriorityAsc(emotionId);
        List<ContentEntity> result = new ArrayList<>();
        for (EmotionContentRecommEntity mapping : mappings) {
            contentRepository.findById(mapping.getContentId()).ifPresent(result::add);
        }
        Collections.shuffle(result);
        return result;
    }
}
