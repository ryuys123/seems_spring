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
@Table(name = "TB_SIMULATION_SETTINGS")
public class SimulationSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "setting_seq_gen")
    @SequenceGenerator(name = "setting_seq_gen", sequenceName = "SETTING_SEQ", allocationSize = 1)
    @Column(name = "SETTING_ID", nullable = false)
    private Long settingId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "SCENARIO_ID", nullable = false)
    private Long scenarioId;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 엔티티의 toDto() 메서드 추가 (필요시)

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "IN_PROGRESS";
        }
    }
}