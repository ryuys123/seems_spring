package com.test.seems.simulation.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSimulationRequest {
    private Long scenarioId;
    private String userId; // 사용자의 ID
}