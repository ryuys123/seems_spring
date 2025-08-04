package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalTestResultRepository extends JpaRepository<PsychologicalTestResultEntity, Long> {

    // 특정 사용자의 가장 최근 결과 1건 조회
    Optional<PsychologicalTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자의 모든 결과 목록을 최신순으로 조회
    List<PsychologicalTestResultEntity> findByUserIdOrderByCreatedAtDesc(String userId);

}