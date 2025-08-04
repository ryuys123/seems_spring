package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.ScaleAnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScaleAnalysisResultRepository extends JpaRepository<ScaleAnalysisResultEntity, Long> {

    // 특정 사용자의 특정 카테고리 최신 결과 조회
    Optional<ScaleAnalysisResultEntity> findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory);

    // 특정 사용자의 모든 카테고리 최신 결과 조회
    Optional<ScaleAnalysisResultEntity> findTopByUser_UserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자의 모든 결과 목록 조회
    List<ScaleAnalysisResultEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId);


}