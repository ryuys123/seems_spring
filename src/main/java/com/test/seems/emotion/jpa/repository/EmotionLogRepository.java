package com.test.seems.emotion.jpa.repository;

import com.test.seems.emotion.jpa.entity.EmotionLog;
import com.test.seems.emotion.jpa.entity.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    List<EmotionLog> findByUserIdOrderByCreatedAtDesc(String userId);
}