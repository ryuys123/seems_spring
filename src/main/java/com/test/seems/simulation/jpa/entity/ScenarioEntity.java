package com.test.seems.simulation.jpa.entity;

import com.test.seems.simulation.model.dto.Simulation;
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
// AuditingEntityListener는 다른 테이블과 유사하게 필요시 추가
// @EntityListeners(AuditingEntityListener.class)
@Table(name = "TB_SIMULATION_SCENARIOS")
public class ScenarioEntity {

    @Id
    // Oracle 시퀀스 사용 예시
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_seq_gen")
    @SequenceGenerator(name = "scenario_seq_gen", sequenceName = "SCENARIO_SEQ", allocationSize = 1)
    @Column(name = "SCENARIO_ID", nullable = false)
    private Long scenarioId;

    @Column(name = "SCENARIO_NAME", nullable = false, length = 100)
    private String scenarioName;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive;

    // LocalDateTime 사용 및 @CreatedDate (Auditing 활성화 시)
    // @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 메서드 (Builder 사용)
    public Simulation toDto() {
        return Simulation.builder()
                .scenarioId(this.scenarioId)
                .scenarioName(this.scenarioName)
                .description(this.description)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = 1;
        }
    }
}