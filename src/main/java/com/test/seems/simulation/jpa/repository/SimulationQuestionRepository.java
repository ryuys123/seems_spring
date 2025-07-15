package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationQuestionRepository extends JpaRepository<SimulationQuestionEntity, Long> {
    // 특정 시나리오와 질문 번호에 해당하는 질문 조회
    List<SimulationQuestionEntity> findByScenarioIdAndQuestionNumber(Long scenarioId, Integer questionNumber);
}