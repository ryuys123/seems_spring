package com.test.seems.content.jpa.repository;

import com.test.seems.content.jpa.entity.EmotionContentRecommEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionContentRecommRepository extends JpaRepository<EmotionContentRecommEntity, EmotionContentRecommEntity.PK> {
    List<EmotionContentRecommEntity> findByEmotionIdOrderByPriorityAsc(Long emotionId);
} 