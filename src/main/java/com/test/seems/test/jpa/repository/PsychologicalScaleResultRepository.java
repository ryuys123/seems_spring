package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalScaleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PsychologicalScaleResultRepository extends JpaRepository<PsychologicalScaleResult, Long> {
    Optional<PsychologicalScaleResult> findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(String userId, String testCategory);
    Optional<PsychologicalScaleResult> findTopByUser_UserIdOrderByCreatedAtDesc(String userId);
}
