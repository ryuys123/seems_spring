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
}