package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.EmotionQuestRecommEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionQuestRecommRepository extends JpaRepository<EmotionQuestRecommEntity, EmotionQuestRecommEntity.PK> {
} 