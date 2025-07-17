package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.UserRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRewardRepository extends JpaRepository<UserRewardEntity, Long> {
    
    /**
     * 사용자가 획득한 뱃지 목록 조회
     */
    List<UserRewardEntity> findByUserId(String userId);
    
    /**
     * 사용자가 특정 뱃지를 보유하고 있는지 확인
     */
    Optional<UserRewardEntity> findByUserIdAndRewardId(String userId, Long rewardId);
    
    /**
     * 사용자가 보유한 뱃지 ID 목록 조회
     */
    @Query("SELECT ur.rewardId FROM UserRewardEntity ur WHERE ur.userId = :userId")
    List<Long> findRewardIdsByUserId(@Param("userId") String userId);
    
    /**
     * 사용자가 보유한 뱃지 개수 조회
     */
    long countByUserId(String userId);
} 