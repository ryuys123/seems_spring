package com.test.seems.simulation.jpa.repository;

import com.test.seems.simulation.jpa.entity.SimulationChoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationChoiceRepository extends JpaRepository<SimulationChoiceEntity, Long> {
    // 특정 세션 ID에 대한 모든 사용자 선택 기록을 질문 번호 순서로 조회
    List<SimulationChoiceEntity> findBySettingIdOrderByQuestionNumberAsc(Long settingId);
}