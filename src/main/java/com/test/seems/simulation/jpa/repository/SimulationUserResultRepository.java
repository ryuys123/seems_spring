// com/test/seems/simulation/jpa/repository/SimulationUserResultRepository.java
package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationUserResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SimulationUserResultRepository extends JpaRepository<SimulationUserResultEntity, Long> {

    // ✅ 사용자 결과 중복 저장을 막기 위해 이 메서드가 필요합니다.
    Optional<SimulationUserResultEntity> findBySettingId(Long settingId);
}