// src/main/java/com/test/seems/test/jpa/repository/PersonalityAnswerRepository.java (파일 이름 변경)
package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PersonalityEntity; // 엔티티는 기존 이름 유지 가정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalityAnswerRepository extends JpaRepository<PersonalityEntity, Long> { // ⭐ 이름 변경
    List<PersonalityEntity> findByUserId(String userId);
    // 특정 사용자의 특정 문항들의 답변을 가져올 때 (MBTI 계산을 위해)
    // List<PersonalityEntity> findByUserIdAndQuestionIdIn(String userId, List<Long> questionIds); // userId 타입 일치 확인
}