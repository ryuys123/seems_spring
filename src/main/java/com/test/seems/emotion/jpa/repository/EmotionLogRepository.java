package com.test.seems.emotion.jpa.repository;

import com.test.seems.emotion.jpa.entity.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    List<EmotionLog> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // 오늘의 감정 조회 (오늘 날짜의 최신 감정)
    @Query(value = "SELECT * FROM TB_EMOTION_LOGS WHERE USER_ID = :userId AND TRUNC(CREATED_AT) = TRUNC(SYSDATE) ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
    Optional<EmotionLog> findTodayEmotionByUserId(@Param("userId") String userId);
    
    // 가장 최근 감정 조회 (오늘 감정이 없을 경우)
    @Query(value = "SELECT * FROM TB_EMOTION_LOGS WHERE USER_ID = :userId ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
    Optional<EmotionLog> findLatestEmotionByUserId(@Param("userId") String userId);
}