package com.test.seems.fortune.jpa.repository;

import com.test.seems.fortune.jpa.entity.UserKeywordsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKeywordsRepository extends JpaRepository<UserKeywordsEntity, Long> {

    /**
     * 사용자의 모든 키워드 상태 조회
     */
    @Query("SELECT u FROM UserKeywordsEntity u WHERE u.userId = :userId")
    List<UserKeywordsEntity> findAllKeywordsByUserId(@Param("userId") String userId);

    /**
     * 특정 키워드의 선택 상태 조회
     */
    @Query("SELECT u FROM UserKeywordsEntity u WHERE u.userId = :userId AND u.guidanceTypeId = :guidanceTypeId")
    Optional<UserKeywordsEntity> findByUserIdAndGuidanceTypeId(@Param("userId") String userId, @Param("guidanceTypeId") Long guidanceTypeId);

    /**
     * 사용자의 선택된 키워드 개수 조회
     */
    @Query("SELECT COUNT(u) FROM UserKeywordsEntity u WHERE u.userId = :userId AND u.isSelected = true")
    long countSelectedKeywordsByUserId(@Param("userId") String userId);
} 