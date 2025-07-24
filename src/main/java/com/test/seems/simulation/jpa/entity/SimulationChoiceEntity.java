package com.test.seems.simulation.jpa.entity;

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



}