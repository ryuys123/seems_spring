package com.test.seems.quest.jpa.repository;

import com.test.seems.quest.jpa.entity.UserPointsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointsRepository extends JpaRepository<UserPointsEntity, String> {
    
    /**
     * 사용자 포인트 정보 조회
     */
    Optional<UserPointsEntity> findByUserId(String userId);
    
    /**
     * 사용자 포인트 차감
     */
    @Modifying
    @Query("UPDATE UserPointsEntity up SET up.points = up.points - :points WHERE up.userId = :userId AND up.points >= :points")
    int deductPoints(@Param("userId") String userId, @Param("points") Integer points);
    
    /**
     * 사용자 포인트 증가
     */
    @Modifying
    @Query("UPDATE UserPointsEntity up SET up.points = up.points + :points WHERE up.userId = :userId")
    int addPoints(@Param("userId") String userId, @Param("points") Integer points);
} 