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
    // User 엔티티 내부에 사용자 ID 필드명이 'userId'일 때 사용합니다.
    List<PsychologicalScaleResult> findByUser_UserIdOrderByCreatedAtDesc(String userId);
    // ✅ 추가: 사용자별 척도 심리검사 모든 결과 조회 (기록 보기용)
    List<PsychologicalScaleResult> findByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory); // ✨ 이 메서드를 추가합니다.
}

