// src/main/java/com/test/seems/test/jpa/repository/ScaleAnalysisResultRepository.java
package com.test.seems.test.jpa.repository;

import com.test.seems.test.model.entity.ScaleAnalysisResultEntity; // 새로 만들 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ScaleAnalysisResultRepository extends JpaRepository<ScaleAnalysisResultEntity, Long> {
    // 특정 사용자 ID와 결과 ID로 척도 검사 결과 조회
    Optional<ScaleAnalysisResultEntity> findByUserIdAndResultId(String userId, Long resultId);

    // 특정 사용자 ID와 테스트 카테고리로 가장 최근 척도 검사 결과 조회
    Optional<ScaleAnalysisResultEntity> findTopByUserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory);

    // ⭐⭐ 이 메서드를 추가하여 오류를 해결합니다. ⭐⭐
    // 특정 사용자 ID의 가장 최근 척도 검사 결과 하나를 조회합니다.
    Optional<ScaleAnalysisResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자 ID의 모든 척도 검사 결과 조회 (최신순)
    List<ScaleAnalysisResultEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);
}