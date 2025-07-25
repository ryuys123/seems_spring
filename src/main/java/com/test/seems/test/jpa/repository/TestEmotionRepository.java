package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.EmotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("testEmotionRepository")
public interface TestEmotionRepository extends JpaRepository<EmotionEntity, Long> {
    Optional<EmotionEntity> findByEmotionName(String emotionName);
}
