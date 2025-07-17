// src/main/java/com/test/seems/test/jpa/repository/PsychologicalImageAnswerRepository.java (파일 이름 변경)
package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologyEntity; // 엔티티는 기존 이름 유지 가정
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsychologicalImageAnswerRepository extends JpaRepository<PsychologyEntity, Long> { // ⭐ 이름 변경
    List<PsychologyEntity> findByUserId(String userId);
    List<PsychologyEntity> findByUserIdOrderByAnswerDatetimeDesc(String userId, Pageable pageable);
}