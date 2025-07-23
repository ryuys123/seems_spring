// com/test/seems/simulation/model/dto/StartSimulationRequest.java
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
    // 일반 시나리오 선택용 필드
    private Long scenarioId;
    // 사용자 식별용 필드
    private String userId;
}