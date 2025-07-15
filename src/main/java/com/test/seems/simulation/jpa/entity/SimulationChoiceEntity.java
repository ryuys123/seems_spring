package com.test.seems.simulation.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_SIMULATION_CHOICES")
public class SimulationChoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "choice_seq_gen")
    @SequenceGenerator(name = "choice_seq_gen", sequenceName = "CHOICE_SEQ", allocationSize = 1)
    @Column(name = "CHOICE_ID")
    private Long choiceId;

    @Column(name = "SETTING_ID", nullable = false)
    private Long settingId; // SimulationSettingEntity 참조

    @Column(name = "QUESTION_NUMBER", nullable = false)
    private Integer questionNumber;

    @Column(name = "CHOICE_TEXT", nullable = false, length = 255)
    private String choiceText; // 사용자가 선택한 텍스트

    // AI 분석을 위해 선택과 연결된 특성(trait) 정보
    @Column(name = "SELECTED_TRAIT", length = 50)
    private String selectedTrait;

    // 엔티티를 DTO로 변환하는 메서드 (Builder 사용)
    // DTO 클래스 (SimulationChoiceDTO) 정의 후 구현 가능
    // public SimulationChoiceDTO toDto() {
    //     return SimulationChoiceDTO.builder()
    //             // ...
    //             .build();
    // }
}