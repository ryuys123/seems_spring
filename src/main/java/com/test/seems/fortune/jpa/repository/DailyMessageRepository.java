package com.test.seems.fortune.jpa.repository;

import com.test.seems.fortune.jpa.entity.DailyMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyMessageRepository extends JpaRepository<DailyMessageEntity, Long> {

    /**
     * 사용자의 오늘 메시지 조회 (중복 데이터 처리)
     */
    @Query("SELECT d FROM DailyMessageEntity d WHERE d.userId = :userId AND d.messageDate = :messageDate ORDER BY d.createdDate DESC")
    List<DailyMessageEntity> findByUserIdAndMessageDate(@Param("userId") String userId, @Param("messageDate") LocalDate messageDate);

    /**
     * 사용자의 메시지 히스토리 조회 (최신순)
     */
    @Query("SELECT d FROM DailyMessageEntity d WHERE d.userId = :userId ORDER BY d.messageDate DESC")
    java.util.List<DailyMessageEntity> findMessageHistoryByUserId(@Param("userId") String userId);
} 