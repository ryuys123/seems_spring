package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.ScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<ScenarioEntity, Long> {
    // 활성화된 시나리오 목록 조회
    List<ScenarioEntity> findByIsActive(Integer isActive);
}