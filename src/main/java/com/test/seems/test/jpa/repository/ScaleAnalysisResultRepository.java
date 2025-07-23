package com.test.seems.test.jpa.repository;

import com.test.seems.test.model.entity.ScaleAnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScaleAnalysisResultRepository extends JpaRepository<ScaleAnalysisResultEntity, Long> {
    Optional<ScaleAnalysisResultEntity> findTopByUserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory);
    List<ScaleAnalysisResultEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    // ⭐ 추가: userId로 가장 최근의 ScaleAnalysisResultEntity를 가져오는 메서드
    Optional<ScaleAnalysisResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
}