package com.test.seems.simulation.model.dto;

import com.test.seems.simulation.jpa.entity.ScenarioEntity;
import com.test.seems.simulation.jpa.entity.SimulationSettingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulation {

    // Scenario 정보를 담기 위한 필드
    private Long scenarioId;
    private String scenarioName;
    private String description;
    private Integer isActive;
    private LocalDateTime createdAt;

    // 세션 정보를 담기 위한 필드
    private Long settingId;
    private String status;
    private String userId;

    // ScenarioEntity를 DTO로 변환하는 정적 메서드 (Builder 사용)
    public static Simulation fromScenarioEntity(ScenarioEntity entity) {
        return Simulation.builder()
                .scenarioId(entity.getScenarioId())
                .scenarioName(entity.getScenarioName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // SimulationSettingEntity를 DTO로 변환하는 정적 메서드 (Builder 사용)
    public static Simulation fromSettingEntity(SimulationSettingEntity entity) {
        return Simulation.builder()
                .settingId(entity.getSettingId())
                .scenarioId(entity.getScenarioId())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}