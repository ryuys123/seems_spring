package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResultEntity, Long> {
    // ✅ 새로운 메서드 추가:
    // resultType이 'TEMPLATE'이고, personalityType이 일치하는 템플릿 정보를 찾습니다.
    Optional<SimulationResultEntity> findByResultTypeAndPersonalityType(String resultType, String personalityType);

}