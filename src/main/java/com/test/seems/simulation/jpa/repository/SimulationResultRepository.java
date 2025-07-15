package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResultEntity, Long> {
    // 특정 세션 ID에 대한 시뮬레이션 결과 조회
    Optional<SimulationResultEntity> findBySettingId(Long settingId);
}