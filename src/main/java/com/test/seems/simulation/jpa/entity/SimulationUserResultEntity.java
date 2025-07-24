// com/test/seems/simulation/jpa/entity/SimulationUserResultEntity.java
package com.test.seems.simulation.jpa.entity;

import com.test.seems.simulation.model.dto.SimulationResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_SIMULATION_USER_RESULTS")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationUserResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_result_id_generator")
    @SequenceGenerator(name = "user_result_id_generator", sequenceName = "TB_SIMULATION_USER_RESULTS_SEQ", allocationSize = 1)
    private Long userResultId;

    @Column(nullable = false, unique = true)
    private Long settingId;

    @Column(nullable = false)
    private String personalityType;

    @Column(nullable = false)
    private String resultTitle;

    @Column(length = 2000)
    private String resultSummary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SimulationResult toDto() {
        return SimulationResult.builder()
                .settingId(this.settingId)
                .personalityType(this.personalityType)
                .resultTitle(this.resultTitle)
                .resultSummary(this.resultSummary)
                .build();
    }
}