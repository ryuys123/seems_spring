package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.QuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<QuestEntity, Long> {
    
    /**
     * 사용자의 모든 퀘스트 조회
     */
    List<QuestEntity> findByUserId(String userId);
    
    /**
     * 사용자의 완료된 퀘스트 조회
     */
    List<QuestEntity> findByUserIdAndIsCompleted(String userId, Integer isCompleted);
    
    /**
     * 사용자의 완료된 퀘스트 조회 (최근활동용)
     */
    List<QuestEntity> findByUserIdAndIsCompletedOrderByCreatedAtDesc(String userId, Integer isCompleted);
    
    /**
     * 사용자의 완료된 퀘스트 개수 조회
     */
    @Query("SELECT COUNT(q) FROM QuestEntity q WHERE q.userId = :userId AND q.isCompleted = 1")
    Long countCompletedQuestsByUserId(@Param("userId") String userId);
    
    /**
     * 사용자의 총 퀘스트 개수 조회
     */
    @Query("SELECT COUNT(q) FROM QuestEntity q WHERE q.userId = :userId")
    Long countTotalQuestsByUserId(@Param("userId") String userId);
} 