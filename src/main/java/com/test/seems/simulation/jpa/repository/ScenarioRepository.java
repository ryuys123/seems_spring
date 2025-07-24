package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.ScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<ScenarioEntity, Long> {
    // 활성화된 시나리오 목록 조회
    List<ScenarioEntity> findByIsActive(Integer isActive);
    // ✅ 추가된 메서드: 시나리오 이름으로 ScenarioEntity를 조회
    Optional<ScenarioEntity> findByScenarioName(String scenarioName);
    Optional<ScenarioEntity> findByDayOfWeekAndIsActive(DayOfWeek dayOfWeek, int isActive);
    // ✅ 이 메서드가 없어서 오류가 발생했습니다.
    // 특정 타입의 활성화된 시나리오 목록을 찾습니다.
    List<ScenarioEntity> findBySimulationTypeAndIsActive(ScenarioEntity.SimulationType simulationType, int isActive);
}