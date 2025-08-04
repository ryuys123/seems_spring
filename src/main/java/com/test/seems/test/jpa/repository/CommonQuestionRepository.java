// src/main/java/com/test/seems/test/jpa/repository/CommonQuestionRepository.java (파일 이름 변경)
package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.TestQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonQuestionRepository extends JpaRepository<TestQuestionEntity, Long> { // ⭐ 이름 변경
    List<TestQuestionEntity> findByTestType(String testType);
    List<TestQuestionEntity> findByTestTypeAndCategory(String testType, String category);
    // 필요한 경우, 특정 유형의 랜덤 질문을 가져오는 쿼리 추가 가능

    // 예: @Query(value = "SELECT * FROM TB_COMMON_QUESTIONS WHERE TEST_TYPE = :testType ORDER BY DBMS_RANDOM.VALUE FETCH FIRST :count ROWS ONLY", nativeQuery = true)
    @Query(value = "SELECT * FROM TB_COMMON_QUESTIONS q WHERE q.TEST_TYPE = :testType ORDER BY DBMS_RANDOM.VALUE FETCH FIRST :count ROWS ONLY", nativeQuery = true)
    List<TestQuestionEntity> findByCategory(String category);
}