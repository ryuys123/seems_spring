// src/main/java/com/test/seems/test/model/entity/ScaleAnalysisResultEntity.java
package com.test.seems.test.model.entity;

import com.test.seems.test.model.dto.PsychologicalTestResultResponse; // 통합 DTO 임포트
import jakarta.persistence.*; // JPA 어노테이션 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_RESULTS") // ⭐ 이 엔티티가 매핑될 테이블명
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 관리를 위해 추가
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScaleAnalysisResultEntity {

    @PrePersist // 엔티티가 영속화되기 전에 호출
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psych_scale_results_seq_gen") // ⭐ 시퀀스 제너레이터 설정
    @SequenceGenerator(name = "psych_scale_results_seq_gen", sequenceName = "SEQ_PSYCH_SCALE_RESULTS_RID", allocationSize = 1) // ⭐ 실제 시퀀스 이름
    @Column(name = "RESULT_ID")
    private Long resultId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "TEST_CATEGORY", nullable = false) // "DEPRESSION_SCALE", "STRESS_SCALE" 등
    private String testCategory;

    @Column(name = "TOTAL_SCORE", nullable = false)
    private Double totalScore; // DDL의 NUMBER(10,2)에 매핑

    @Lob // CLOB 타입 매핑 (대용량 텍스트)
    @Column(name = "INTERPRETATION")
    private String interpretation;

    @Column(name = "RISK_LEVEL")
    private String riskLevel;

    @Lob // CLOB 타입 매핑 (대용량 텍스트)
    @Column(name = "SUGGESTIONS")
    private String suggestions;

    @CreatedDate // 스프링 데이터 JPA의 Auditing 기능으로 생성 시간 자동 관리
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 👇 Add this field and annotation
    @Column(name = "TEST_TYPE", nullable = false)
    private String testType;

    // 이 엔티티를 PsychologicalTestResultResponse DTO로 변환하는 메서드
    public PsychologicalTestResultResponse toDto() {
        return PsychologicalTestResultResponse.builder()
                .resultId(this.resultId)
                .userId(this.userId)
                // 척도 검사 결과는 questionId, rawResponseText, AI 분석 필드들이 null일 수 있음
                .questionId(null)
                .rawResponseText(null)
                .testType(this.testType)           // 엔티티의 testType을 올바르게 매핑
                .diagnosisCategory(this.testCategory) // 진단 카테고리에 testCategory를 매핑
                // 척도 검사 특화 필드
                .totalScore(this.totalScore)
                .interpretationText(this.interpretation)
                .riskLevel(this.riskLevel)
                // AI 분석 관련 필드는 척도 검사에서 AI 분석을 직접 하지 않는다면 null로 설정
                .aiSentiment(null)
                .aiSentimentScore(null)
                .aiCreativityScore(null)
                .aiPerspectiveKeywords(null)
                .aiInsightSummary(null)
                .suggestions(this.suggestions)
                .testDateTime(this.createdAt)
                .build();
    }
}