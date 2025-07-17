// src/main/java/com/test/seems/test/jpa/repository/PsychologicalScaleAnswerRepository.java
package com.test.seems.test.jpa.repository;

import com.test.seems.test.model.entity.ScaleTestAnswerEntity; // 새로 만들 엔티티 임포트 (예시 이름)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsychologicalScaleAnswerRepository extends JpaRepository<ScaleTestAnswerEntity, Long> {
    List<ScaleTestAnswerEntity> findByUserIdAndTestCategoryOrderByAnswerDatetimeDesc(String userId, String testCategory);
    // 기타 필요한 쿼리 메서드
}