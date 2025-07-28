// src/main/java/com/test/seems/simulation/model/dto/SimulationResultDetails.java
package com.test.seems.simulation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter, Setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 포함하는 생성자 자동 생성
public class SimulationResultDetails {
    private String resultTitle;
    private String resultSummary;
    private String positiveContributionFactors;
    private int initialStressScore;
    private int estimatedFinalStressScore;
    private int initialDepressionScore;
    private int estimatedFinalDepressionScore;

    // 필요하다면 추가적인 필드를 여기에 정의할 수 있습니다.
}