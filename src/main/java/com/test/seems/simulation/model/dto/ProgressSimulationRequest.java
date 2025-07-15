package com.test.seems.simulation.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressSimulationRequest {
    private Long settingId;
    private Integer questionNumber;
    private String choiceText;
    private String selectedTrait;
}