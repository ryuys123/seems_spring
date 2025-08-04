package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // List 임포트 추가
import java.util.Optional;

@Repository
public interface PersonalityTestResultRepository extends JpaRepository<PersonalityTestResultEntity, Long> {

    // 가장 최근 검사 결과 1건 조회
    Optional<PersonalityTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자의 모든 검사 결과 목록을 최신순으로 조회
    List<PersonalityTestResultEntity> findByUserIdOrderByCreatedAtDesc(String userId);


}