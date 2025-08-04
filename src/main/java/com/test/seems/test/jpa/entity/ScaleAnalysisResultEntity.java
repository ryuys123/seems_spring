package com.test.seems.test.jpa.entity;

import com.test.seems.test.model.dto.PsychologicalTestResultResponse;
import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp; // ✨ 임포트 추가
import java.time.LocalDateTime; // ✨ Timestamp 대신 LocalDateTime 사용

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_RESULTS")
@Data
public class ScaleAnalysisResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESULT_ID")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, referencedColumnName = "USER_ID")
    private UserEntity user;

    @Column(name = "TEST_CATEGORY", nullable = false)
    private String testCategory;

    @Column(name = "TOTAL_SCORE", nullable = false)
    private Double totalScore;

    @Lob
    @Column(name = "INTERPRETATION")
    private String interpretation;

    @Column(name = "RISK_LEVEL")
    private String riskLevel;

    @Lob
    @Column(name = "SUGGESTIONS")
    private String suggestions;

    // ✨ [수정 후]
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 타입을 LocalDateTime으로 변경

    // ✨ PsychologicalScaleResult로부터 가져온 필드
    @Column(name = "TEST_TYPE")
    private String testType;

    // ✨ PsychologicalScaleResult로부터 가져온 toDto 메서드
    public PsychologicalTestResultResponse toDto() {
        return PsychologicalTestResultResponse.builder()
                .resultId(this.resultId)
                .userId(this.user.getUserId())
                .testType(this.testType)
                .diagnosisCategory(this.testCategory) // testCategory를 diagnosisCategory에 매핑
                .totalScore(this.totalScore)
                .interpretationText(this.interpretation)
                .riskLevel(this.riskLevel)
                .suggestions(this.suggestions)
                // ✨ [수정] createdAt 필드가 이미 LocalDateTime이므로 .toLocalDateTime() 호출을 제거합니다.
                .testDateTime(this.createdAt)
                .build();
    }
}