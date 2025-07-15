package com.test.seems.simulation.model.dto;

import com.test.seems.simulation.jpa.entity.SimulationResultEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResultDTO {

    private Long resultId;
    private Long settingId;
    private String resultSummary; // AI 분석 요약
    private String personalityType; // 성격 유형
    private LocalDateTime createdAt;

    // 엔티티에서 DTO로 변환하는 메서드
    public static SimulationResultDTO fromEntity(SimulationResultEntity entity) {
        return SimulationResultDTO.builder()
                .resultId(entity.getResultId())
                .settingId(entity.getSettingId())
                .resultSummary(entity.getResultSummary())
                .personalityType(entity.getPersonalityType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}