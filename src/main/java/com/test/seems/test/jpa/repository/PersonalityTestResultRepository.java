package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalityTestResultRepository extends JpaRepository<PersonalityTestResultEntity, Long> {

    // 사용자의 가장 최근 검사 결과를 가져오는 쿼리 메서드
    Optional<PersonalityTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
    // ✨ 특정 사용자의 모든 결과를 최신순으로 가져오는 메서드 (새로 추가)
    List<PersonalityTestResultEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);
}