package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.TestQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestionEntity, Long> {
    List<TestQuestionEntity> findByTestType(String testType);
    List<TestQuestionEntity> findByTestTypeAndCategory(String testType, String category);
    List<TestQuestionEntity> findByTestTypeIgnoreCaseAndCategoryIgnoreCase(String testType, String category);
}
