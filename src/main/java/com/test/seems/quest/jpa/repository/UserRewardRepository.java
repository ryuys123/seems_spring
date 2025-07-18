package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.UserRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 해당 유저의 모든 뱃지 isEquipped=0으로 초기화
     */
    @Modifying
    @Query("UPDATE UserRewardEntity ur SET ur.isEquipped = 0 WHERE ur.userId = :userId")
    void updateAllEquippedToFalse(@Param("userId") String userId);

    /**
     * 특정 뱃지 isEquipped=1로 변경
     */
    @Modifying
    @Query("UPDATE UserRewardEntity ur SET ur.isEquipped = :isEquipped WHERE ur.userId = :userId AND ur.rewardId = :rewardId")
    void updateEquippedByUserIdAndRewardId(@Param("userId") String userId, @Param("rewardId") Long rewardId, @Param("isEquipped") Integer isEquipped);

    /**
     * 장착중인 뱃지 1개 조회
     */
    Optional<UserRewardEntity> findFirstByUserIdAndIsEquipped(String userId, Integer isEquipped);
} 