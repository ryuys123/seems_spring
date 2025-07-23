package com.test.seems.simulation.jpa.entity;

import com.test.seems.simulation.model.dto.SimulationResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_SIMULATION_RESULTS")
public class SimulationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_id_generator")
    @SequenceGenerator(name = "result_id_generator", sequenceName = "TB_SIMULATION_RESULTS_SEQ", allocationSize = 1)
    private Long resultId;

    // 'TEMPLATE' 또는 'USER_RESULT' 같은 값을 저장할 컬럼
    @Column(name = "RESULT_TYPE", nullable = false)
    private String resultType;

    // 템플릿인 경우, 어떤 성향에 대한 템플릿인지 저장 (예: 'CREATIVE', 'LOGICAL')
    // 사용자 결과인 경우, 사용자가 최종적으로 부여받은 성향을 저장
    @Column(name = "PERSONALITY_TYPE")
    private String personalityType;

    @Column(name = "RESULT_TITLE")
    private String resultTitle;

    @Column(name = "RESULT_SUMMARY", length = 1000)
    private String resultSummary;



    public SimulationResult toDto() {
        return SimulationResult.builder()
                .resultTitle(this.resultTitle)
                .resultSummary(this.resultSummary)
                .personalityType(this.personalityType)
                .build();
    }
}