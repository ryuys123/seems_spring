package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationSettingRepository extends JpaRepository<SimulationSettingEntity, Long> {
    // 특정 사용자의 최근 진행 세션 조회 (중간 저장 기능에 사용)
    Optional<SimulationSettingEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 상태의 세션 목록 조회
    List<SimulationSettingEntity> findByStatus(String status);
    // ✅ 'cannot find symbol' 오류 해결을 위해 이 메소드 선언을 추가해야 합니다.
    Optional<SimulationSettingEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    Optional<SimulationSettingEntity> findByUserIdAndStatus(String userId, String status);
    // ✅ 시뮬레이션 서비스의 getLatestSimulationResult에서 사용될 메서드 추가
    // userId에 해당하는 가장 최근 COMPLETED 상태의, scenarioId가 NULL인 (AI 생성) Setting을 찾습니다.
    Optional<SimulationSettingEntity> findTopByUserIdAndStatusAndScenarioIdIsNullOrderByCreatedAtDesc(String userId, String status);
}