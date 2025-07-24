package com.test.seems.counseling.jpa.repository;

import com.test.seems.counseling.jpa.entity.CounselingAnalysisSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounselingAnalysisSummaryRepository extends JpaRepository<CounselingAnalysisSummaryEntity, Long> {
    // 특정 사용자의 상담 분석 요약을 조회하는 메서드 (필요시 추가)
    // List<CounselingAnalysisSummaryEntity> findBySession_User_UserId(String userId);

    // 최신 상담 분석 요약을 가져오는 메서드 추가
    Optional<CounselingAnalysisSummaryEntity> findTopBySession_User_UserIdOrderByCreatedAtDesc(String userId);
}