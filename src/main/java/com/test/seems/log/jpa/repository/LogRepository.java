package com.test.seems.log.jpa.repository;

import com.test.seems.log.jpa.entity.LogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {

    int countByActionContainingIgnoreCase(String action);
    int countBySeverityContainingIgnoreCase(String severity);
    int countByCreatedAtBetween(LocalDateTime begin, LocalDateTime  end);

    List<LogEntity> findByActionContainingIgnoreCase(String action, Pageable pageable);
    List<LogEntity> findBySeverityContainingIgnoreCase(String severity, Pageable pageable);
    List<LogEntity> findByCreatedAtBetween(LocalDateTime  start, LocalDateTime  end, Pageable pageable);

    // 로그인 실패 로그만 userId 기준으로 최근 10분 내 카운트
    @Query("""
    SELECT COUNT(l)
    FROM LogEntity l
    WHERE l.userId = :userId
      AND l.action = '로그인 시도'
      AND l.severity IN ('WARN', 'CRITICAL')
      AND l.createdAt >= :since
""")
    int countRecentFailedLogins(@Param("userId") String userId, @Param("since") LocalDateTime since);

    // admin 대시보드 - 방문자통계


    // 일별 방문자 수
    @Query("""
SELECT NEW map(
  FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM-DD') AS period,
  COUNT(DISTINCT l.userId) AS visitorCount
)
FROM LogEntity l
WHERE l.action = '로그인 시도' AND l.severity = 'INFO'
GROUP BY FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM-DD')
ORDER BY FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM-DD')
""")
    List<Map<String, Object>> getDailyVisitorStats();


    // 주별 방문자 수
    @Query("""
SELECT NEW map(
  FUNCTION('TO_CHAR', l.createdAt, 'IYYY-IW') AS period,
  COUNT(DISTINCT l.userId) AS visitorCount
)
FROM LogEntity l
WHERE l.action = '로그인 시도' AND l.severity = 'INFO'
GROUP BY FUNCTION('TO_CHAR', l.createdAt, 'IYYY-IW')
ORDER BY FUNCTION('TO_CHAR', l.createdAt, 'IYYY-IW')
""")
    List<Map<String, Object>> getWeeklyVisitorStats();


    // 월별 방문자 수
    @Query("""
SELECT NEW map(
  FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM') AS period,
  COUNT(DISTINCT l.userId) AS visitorCount
)
FROM LogEntity l
WHERE l.action = '로그인 시도' AND l.severity = 'INFO'
GROUP BY FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM')
ORDER BY FUNCTION('TO_CHAR', l.createdAt, 'YYYY-MM')
""")
    List<Map<String, Object>> getMonthlyVisitorStats();

}
