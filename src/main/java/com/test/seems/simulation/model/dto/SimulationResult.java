// com/test/seems/simulation/model/dto/SimulationResultDTO.java
package com.test.seems.simulation.model.dto;

import com.test.seems.simulation.jpa.entity.SimulationResultEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResult {
    private String resultTitle;        // ✅ PERSONALITY_TYPE에 매핑될 필드 (예: "냉철한 현실주의자")
    private String resultSummary;      // ✅ RESULT_SUMMARY에 매핑될 필드 (예: "당신은 감상에 빠지기보다...")
    private String personalityType;    // ✅ (선택적) 성향 키워드 (예: "REALISM", "CAUTION")
    private Long settingId;

    // DTO를 Entity로 변환하는 메서드 (DB 저장을 위해 필요)
    public SimulationResultEntity toEntity() {
        return SimulationResultEntity.builder()
                .resultSummary(this.resultSummary)
                .personalityType(this.personalityType) // 이 필드가 PERSONALITY_TYPE 컬럼에 저장
                .build();
    }
}