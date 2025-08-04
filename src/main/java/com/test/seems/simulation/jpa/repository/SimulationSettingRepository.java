package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationSettingRepository extends JpaRepository<SimulationSettingEntity, Long> {

    // ✅ 'cannot find symbol' 오류 해결을 위해 이 메소드 선언을 추가해야 합니다.
    Optional<SimulationSettingEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    Optional<SimulationSettingEntity> findByUserIdAndStatus(String userId, String status);

}