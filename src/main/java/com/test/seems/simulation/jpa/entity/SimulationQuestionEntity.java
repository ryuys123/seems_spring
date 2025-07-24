package com.test.seems.simulation.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

// DTO 변환을 위한 import는 DTO 클래스 정의 후 추가합니다.
// import com.test.seems.simulation.model.dto.SimulationQuestionDTO;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_SIMULATION_QUESTIONS")
public class SimulationQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq_gen")
    @SequenceGenerator(name = "question_seq_gen", sequenceName = "QUESTION_SEQ", allocationSize = 1)
    @Column(name = "QUESTION_ID")
    private Long questionId;

    @Column(name = "SCENARIO_ID", nullable = false)
    private Long scenarioId;

    @Column(name = "QUESTION_NUMBER", nullable = false)
    private Integer questionNumber;

    // 긴 텍스트이므로 @Lob을 고려할 수 있지만, VARCHAR2(2000)이므로 일단 유지
    @Column(name = "QUESTION_TEXT", nullable = false, length = 2000)
    private String questionText;

    // 선택지 옵션 (AI 응답 결과 JSON 저장용)
    @Column(name = "CHOICE_OPTIONS", nullable = false, length = 2000)
    private String choiceOptions;

    // ✅ 추가된 컬럼: SETTING_ID
    @Column(name = "SETTING_ID", nullable = false)
    private Long settingId;

    // 엔티티를 DTO로 변환하는 메서드 (Builder 사용)
    // DTO 클래스 (SimulationQuestionDTO) 정의 후 구현 가능
    // public SimulationQuestionDTO toDto() {
    //     return SimulationQuestionDTO.builder()
    //             .questionId(this.questionId)
    //             // ...
    //             .build();
    // }
}