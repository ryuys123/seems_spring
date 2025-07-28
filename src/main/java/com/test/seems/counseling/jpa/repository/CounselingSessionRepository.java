
package com.test.seems.counseling.jpa.repository;

import com.test.seems.adminDashboard.model.dto.CounselingStats;
import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import com.test.seems.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSessionEntity, Long> {
    List<CounselingSessionEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    Optional<CounselingSessionEntity> findTopByUserOrderByCreatedAtDesc(UserEntity user);


    // admin 대시보드 상담 통계
    @Query("""
    SELECT FUNCTION('TO_CHAR', c.createdAt, 'IYYY-IW') AS week,
           COUNT(c.sessionId) AS count
    FROM CounselingSessionEntity c
    GROUP BY FUNCTION('TO_CHAR', c.createdAt, 'IYYY-IW')
    ORDER BY FUNCTION('TO_CHAR', c.createdAt, 'IYYY-IW')
""")
    List<Map<String, Object>> getWeeklyCounselingStats();

    @Query("SELECT COUNT(c) FROM CounselingSessionEntity c")
    long countTotalCounselingLogs();
}

