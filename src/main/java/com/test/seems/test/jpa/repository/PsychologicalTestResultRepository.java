package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity; // 매핑될 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalTestResultRepository extends JpaRepository<PsychologicalTestResultEntity, Long> {
    /**
     * 특정 사용자 ID에 해당하는 모든 심리 분석 결과를 조회합니다.
     * @param userId 사용자 ID (String 타입)
     * @return 해당 사용자의 PsychologicalTestResultEntity 리스트
     */
    List<PsychologicalTestResultEntity> findByUserId(String userId); // USER_ID가 String 타입

    /**
     * 특정 사용자 ID에 해당하는 가장 최신 심리 분석 결과 하나를 조회합니다.
     * @param userId 사용자 ID (String 타입)
     * @return Optional<PsychologicalTestResultEntity> (결과가 없을 수도 있음)
     */
    Optional<PsychologicalTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId); // CreatedAt 기준으로 최신 정렬

    /**
     * 결과 ID로 특정 심리 분석 결과 하나를 조회합니다.
     * @param resultId 결과 고유 식별자
     * @return Optional<PsychologicalTestResultEntity>
     */
    Optional<PsychologicalTestResultEntity> findByResultId(Long resultId);

    /**
     * 특정 사용자 ID와 결과 ID로 심리 분석 결과 하나를 조회합니다.
     * @param userId 사용자 ID
     * @param resultId 결과 고유 식별자
     * @return Optional<PsychologicalTestResultEntity>
     */
    Optional<PsychologicalTestResultEntity> findByUserIdAndResultId(String userId, Long resultId);

    /**
     * 특정 사용자의 가장 최근 심리 분석 결과를 찾습니다.
     * (createdAt 기준 내림차순으로 정렬하여 첫 번째(Top 1) 결과를 가져옵니다.)
     */
    Optional<PsychologicalTestResultEntity> findTop1ByUserIdOrderByCreatedAtDesc(String userId);
}


