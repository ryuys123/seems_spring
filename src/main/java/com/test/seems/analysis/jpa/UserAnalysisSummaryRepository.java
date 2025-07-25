package com.test.seems.analysis.jpa;

import com.test.seems.analysis.jpa.entity.UserAnalysisSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAnalysisSummaryRepository extends JpaRepository<UserAnalysisSummaryEntity, Long> {
    Optional<UserAnalysisSummaryEntity> findByUserId(String userId);
}
