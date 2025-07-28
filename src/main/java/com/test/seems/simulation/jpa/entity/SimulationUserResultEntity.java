// D:\Finalworkspace\backend\src\main\java\com\test\seems\simulation\jpa\entity\SimulationUserResultEntity.java

package com.test.seems.simulation.jpa.entity;

import com.test.seems.simulation.model.dto.SimulationResult; // DTO 임포트 확인
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "TB_SIMULATION_USER_RESULTS")
@DynamicInsert
@DynamicUpdate
public class SimulationUserResultEntity {

    @Id
    // ✅ 여기 @GeneratedValue 설정이 중요합니다.
    // DDL에서 시퀀스 + 트리거 방식을 사용한다면, 아래처럼 설정합니다.
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_result_seq_gen")
    @SequenceGenerator(name = "user_result_seq_gen", sequenceName = "SEQ_TB_SIMULATION_USER_RESULTS_ID", allocationSize = 1)
    // 만약 DB에서 IDENTITY 컬럼을 사용한다면 GenerationType.IDENTITY로 설정합니다.
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_result_id")
    private Long userResultId;

    @Column(name = "setting_id", nullable = false, unique = true)
    private Long settingId;

    // ✨ 기존 필드: AI가 제공하는 요약, 제목
    @Column(name = "result_title", nullable = false, length = 255)
    private String resultTitle; // 최종 제안/결과 제목

    @Column(name = "result_summary", nullable = false, length = 2000)
    private String resultSummary; // 최종 분석 요약

    // ✨ 삭제할 필드 (성향 타입은 사용하지 않으므로)
    // @Column(name = "personality_type", length = 100)
    // private String personalityType;

    // ✨ 새로 추가할 필드들 ✨
    @Column(name = "initial_stress_score")
    private Integer initialStressScore; // 시뮬레이션 시작 전 스트레스 검사 점수

    @Column(name = "initial_depression_score")
    private Integer initialDepressionScore; // 시뮬레이션 시작 전 우울감 검사 점수

    @Column(name = "estimated_final_stress_score")
    private Integer estimatedFinalStressScore; // 시뮬레이션 후 AI가 추정한 스트레스 점수

    @Column(name = "estimated_final_depression_score")
    private Integer estimatedFinalDepressionScore; // 시뮬레이션 후 AI가 추정한 우울감 점수

    @Column(name = "positive_contribution_factors", length = 2000)
    private String positiveContributionFactors; // AI가 분석한 긍정적 기여 요인 (텍스트 요약)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- DTO 변환 메소드 (SimulationResult DTO에 맞춰 필드 추가 예정) ---
    public SimulationResult toDto() {
        return SimulationResult.builder()
                .userResultId(this.userResultId)
                .settingId(this.settingId)
                .resultTitle(this.resultTitle)
                .resultSummary(this.resultSummary)
                // ✨ DTO에도 추가해야 할 필드들
                .initialStressScore(this.initialStressScore)
                .initialDepressionScore(this.initialDepressionScore)
                .estimatedFinalStressScore(this.estimatedFinalStressScore)
                .estimatedFinalDepressionScore(this.estimatedFinalDepressionScore)
                .positiveContributionFactors(this.positiveContributionFactors)
                .createdAt(this.createdAt)
                .build();
    }
}