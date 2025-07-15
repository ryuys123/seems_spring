package com.test.seems.simulation.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_SIMULATION_RESULTS")
public class SimulationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_sim_seq_gen")
    @SequenceGenerator(name = "result_sim_seq_gen", sequenceName = "SIM_RESULT_SEQ", allocationSize = 1)
    @Column(name = "RESULT_ID")
    private Long resultId;

    @Column(name = "SETTING_ID", nullable = false)
    private Long settingId;

    @Lob // CLOB 타입 매핑
    @Column(name = "RESULT_SUMMARY", nullable = false, length = 2000)
    private String resultSummary; // AI가 생성한 성격 분석 요약

    @Column(name = "PERSONALITY_TYPE", length = 50)
    private String personalityType;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 엔티티의 toDto() 메서드 추가 (필요시)

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}