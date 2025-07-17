// src/main/java/com/test/seems/test/jpa/repository/PsychologicalImageResultRepository.java (파일 이름 변경)
package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity; // 엔티티는 기존 이름 유지 가정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalImageResultRepository extends JpaRepository<PsychologicalTestResultEntity, Long> { // ⭐ 이름 변경
    List<PsychologicalTestResultEntity> findByUserId(String userId);
    Optional<PsychologicalTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
    Optional<PsychologicalTestResultEntity> findByResultId(Long resultId);
    Optional<PsychologicalTestResultEntity> findByUserIdAndResultId(String userId, Long resultId);
    Optional<PsychologicalTestResultEntity> findTop1ByUserIdOrderByCreatedAtDesc(String userId);
}