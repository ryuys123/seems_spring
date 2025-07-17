package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.QuestRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRewardRepository extends JpaRepository<QuestRewardEntity, Long> {
    
    /**
     * 모든 뱃지 보상 목록 조회
     */
    List<QuestRewardEntity> findAllByOrderByRequiredPointsAsc();
    
    /**
     * 특정 레어리티의 뱃지 보상 목록 조회
     */
    List<QuestRewardEntity> findByRewardRarityOrderByRequiredPointsAsc(String rewardRarity);
} 