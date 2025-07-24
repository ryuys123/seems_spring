package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationQuestionRepository extends JpaRepository<SimulationQuestionEntity, Long> {
    // 특정 시나리오와 질문 번호에 해당하는 질문 조회
    Optional<SimulationQuestionEntity> findByScenarioIdAndQuestionNumber(Long scenarioId, Integer questionNumber);
    // ✅ 지금 오류 해결을 위해 추가해야 할 메소드
    boolean existsBySettingIdAndQuestionNumber(Long settingId, Integer questionNumber);
    // ✅ 'cannot find symbol' 오류 해결을 위해 이 메소드 선언이 필요합니다.
    Optional<SimulationQuestionEntity> findBySettingIdAndQuestionNumber(Long settingId, Integer questionNumber);
    // ✅ [새로운 메서드 추가]
    // 시나리오 ID를 기준으로, 질문 번호(QuestionNumber)를 오름차순(Asc)으로 정렬하여
    // 가장 첫 번째(findFirst) 질문을 찾아옵니다.
    Optional<SimulationQuestionEntity> findFirstByScenarioIdOrderByQuestionNumberAsc(Long scenarioId);

}