package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PersonalityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalityRepository extends JpaRepository<PersonalityEntity, Long> {
    List<PersonalityEntity> findByUserId(Long userId);
    // 특정 사용자의 특정 문항들의 답변을 가져올 때 (MBTI 계산을 위해)
    // List<PersonalityAnswerEntity> findByUserIdAndQuestionIdIn(Long userId, List<Long> questionIds);
}