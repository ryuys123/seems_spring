package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologyEntity; // 매핑될 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsychologyRepository extends JpaRepository<PsychologyEntity, Long> { // <<-- PsychologyEntity에 매핑
    /**
     * 특정 사용자 ID에 해당하는 모든 심리 검사 답변을 조회합니다.
     * @param userId 사용자 ID (String 타입)
     * @return 해당 사용자의 PsychologyEntity 리스트
     */
    List<PsychologyEntity> findByUserId(String userId); // USER_ID가 String 타입 (UserEntity의 userId와 일치)
}