// D:\Finalworkspace\backend\src\main\java\com\test\seems\simulation\model\dto\SimulationResult.java

package com.test.seems.simulation.model.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SimulationResult {
    private Long userResultId;
    private Long settingId;
    private String resultTitle;
    private String resultSummary;
    // ✨ 삭제할 필드
    // private String personalityType;

    // ✨ 새로 추가할 필드들 ✨
    private Integer initialStressScore;
    private Integer initialDepressionScore;
    private Integer estimatedFinalStressScore;
    private Integer estimatedFinalDepressionScore;
    private String positiveContributionFactors; // 긍정적 기여 요인

    private LocalDateTime createdAt;
}