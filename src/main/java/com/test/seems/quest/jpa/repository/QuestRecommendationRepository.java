package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.QuestRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestRecommendationRepository extends JpaRepository<QuestRecommendationEntity, Long> {
    @Query("SELECT q FROM QuestRecommendationEntity q JOIN EmotionQuestRecommEntity e ON q.recommendId = e.recommendId WHERE e.emotionId = :emotionId ORDER BY e.priority ASC")
    List<QuestRecommendationEntity> findByEmotionId(@Param("emotionId") Long emotionId);

    @Query(value = "SELECT * FROM ( " +
            "SELECT q.* FROM TB_QUEST_RECOMMENDATIONS q " +
            "JOIN TB_EMOTION_QUEST_RECOMM eqr ON q.RECOMMEND_ID = eqr.RECOMMEND_ID " +
            "WHERE eqr.EMOTION_ID = ( " +
            "  SELECT EMOTION_ID FROM TB_EMOTION_LOGS WHERE USER_ID = :userId ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY " +
            ") " +
            "ORDER BY DBMS_RANDOM.VALUE " +
            ") WHERE ROWNUM <= 6", nativeQuery = true)
    List<QuestRecommendationEntity> findRandom6ByUserIdLatestEmotion(@Param("userId") String userId);
} 