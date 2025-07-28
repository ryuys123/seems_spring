package com.test.seems.simulation.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data; // @Data 사용으로 Getter, Setter, RequiredArgsConstructor, ToString, EqualsAndHashCode 자동 포함
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data // @Getter, @Setter, @RequiredArgsConstructor, @ToString, @EqualsAndHashCode 포함
@Entity
@NoArgsConstructor // JPA를 위한 기본 생성자
@AllArgsConstructor // @Builder와 함께 사용될 때 모든 필드를 인자로 받는 생성자
@Builder // 빌더 패턴을 사용하여 객체 생성 가능
@Table(name = "TB_SIMULATION_SETTINGS") // 실제 테이블 이름과 일치하는지 확인 (이전에 TB_SIMULATION_SETTING으로 논의했지만, 여기선 TB_SIMULATION_SETTINGS)
public class SimulationSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "setting_seq_gen")
    @SequenceGenerator(name = "setting_seq_gen", sequenceName = "SETTING_SEQ", allocationSize = 1)
    @Column(name = "SETTING_ID", nullable = false)
    private Long settingId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    // 극복 시뮬레이션의 경우 scenarioId가 없을 수 있으므로 nullable을 true로 변경하거나
    // @Column(name = "SCENARIO_ID", nullable = true) 등으로 조정 필요
    // 현재 종합 분석 기반의 극복 시뮬레이션은 scenarioId가 null이 될 수 있습니다.
    @Column(name = "SCENARIO_ID", nullable = true) // ✅ AI 생성 시나리오를 위해 nullable을 true로 변경
    private Long scenarioId;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✨ 극복 시뮬레이션 진행 상황 추적을 위한 필드 추가 ✨
    @Column(name = "CURRENT_QUESTION_NUMBER")
    private Integer currentQuestionNumber; // 현재 진행 중인 질문의 번호 (1부터 시작)

    @Column(name = "TOTAL_QUESTIONS_COUNT")
    private Integer totalQuestionsCount; // 이 시뮬레이션의 총 질문 개수 (예: 7)

    @Column(name = "INITIAL_STRESS_SCORE") // 사용자 종합 분석에서 가져온 초기 스트레스 점수
    private Integer initialStressScore;

    @Column(name = "INITIAL_DEPRESSION_SCORE") // 사용자 종합 분석에서 가져온 초기 우울감 점수
    private Integer initialDepressionScore;


    @PrePersist // 엔티티가 영속화되기 전에 호출되는 콜백
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "IN_PROGRESS"; // 기본 상태 설정
        }
        // 극복 시뮬레이션 시작 시점에서 이 값들이 Service에서 설정되지만,
        // 혹시 null일 경우를 대비한 안전 장치 (여기서는 기본값 0 또는 1로 설정할 수도 있음)
        if (currentQuestionNumber == null) {
            currentQuestionNumber = 0; // 혹은 1로 시작 시점에 세팅
        }
        // totalQuestionsCount는 Service에서 명시적으로 설정되므로 여기서는 필요 없을 수 있음
    }

    // ✨ 누락되었던 현재 질문 번호 증가 메서드 추가 ✨
    public void incrementCurrentQuestionNumber() {
        if (this.currentQuestionNumber == null) {
            this.currentQuestionNumber = 1; // 안전 장치: null이면 1로 시작
        } else {
            this.currentQuestionNumber++; // 1씩 증가
        }
    }
}