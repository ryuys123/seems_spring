package com.test.seems.emotion.jpa.repository;

import com.test.seems.emotion.jpa.entity.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    // 사용자별 감정 로그 조회 (최근활동용) - userId 속성 사용
    List<EmotionLog> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // 오늘의 감정 조회 (오늘 날짜의 최신 감정)
    @Query(value = "SELECT * FROM TB_EMOTION_LOGS WHERE USER_ID = :userId AND TRUNC(CREATED_AT) = TRUNC(SYSDATE) ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
    Optional<EmotionLog> findTodayEmotionByUserId(@Param("userId") String userId);
    
    // 가장 최근 감정 조회 (오늘 감정이 없을 경우)
    @Query(value = "SELECT * FROM TB_EMOTION_LOGS WHERE USER_ID = :userId ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
    Optional<EmotionLog> findLatestEmotionByUserId(@Param("userId") String userId);

    // admin 대시보드 감정기록 통계
    @Query("""
    SELECT FUNCTION('TO_CHAR', e.createdAt, 'IYYY-IW') AS week,
           COUNT(e.emotionLogId) AS count
    FROM EmotionLog e
    GROUP BY FUNCTION('TO_CHAR', e.createdAt, 'IYYY-IW')
    ORDER BY FUNCTION('TO_CHAR', e.createdAt, 'IYYY-IW')
""")
    List<Map<String, Object>> getWeeklyEmotionStats();

    @Query("SELECT COUNT(e) FROM EmotionLog e")
    long countTotalEmotionLogs();
}