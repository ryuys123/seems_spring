package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalScaleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalScaleResultRepository extends JpaRepository<PsychologicalScaleResult, Long> {
    Optional<PsychologicalScaleResult> findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory);
    Optional<PsychologicalScaleResult> findTopByUser_UserIdOrderByCreatedAtDesc(String userId);
    boolean existsByUser_UserIdAndTestCategory(String userId, String testCategory);
    
    // 사용자별 척도 심리검사 결과 조회 (최근활동용)
    List<PsychologicalScaleResult> findByUser_UserIdOrderByCreatedAtDesc(String userId);
}
