package com.test.seems.analysis.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data // ✨ @Data 어노테이션 적용: @Getter, @Setter, @RequiredArgsConstructor, @ToString, @EqualsAndHashCode 포함
@Builder // ✨ @Builder 어노테이션 유지 (빌더 패턴 사용을 위해 필요)
@NoArgsConstructor // ✨ @NoArgsConstructor 어노테이션 유지 (JPA 기본 생성자 필요)
@AllArgsConstructor // ✨ @AllArgsConstructor 어노테이션 유지 (@Builder와 함께 사용 시 유용)
@Table(name = "TB_USER_ANALYSIS_SUMMARY")
public class UserAnalysisSummaryEntity {

    @Id
    @Column(name = "USER_SUMMARY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_summary_seq")
    @SequenceGenerator(name = "user_summary_seq", sequenceName = "SEQ_USER_ANALYSIS_SUMMARY_USER_SUMMARY_ID", allocationSize = 1)
    private Long userSummaryId;

    @Column(name = "USER_ID", length = 255)
    private String userId;

    @Column(name = "LAST_UPDATED")
    @Temporal(TemporalType.DATE)
    private Date lastUpdated;

    @Column(name = "PSYCHO_IMAGE_RESULT_ID")
    private Long psychoImageResultId;

    @Column(name = "PERSONALITY_RESULT_ID")
    private Long personalityResultId;

    @Column(name = "PSYCHO_SCALE_RESULT_ID")
    private Long psychoScaleResultId;

    @Column(name = "EMOTION_ID")
    private Long emotionId;

    @Column(name = "SUMMARY_ID") // CounselingAnalysisSummaryEntity's summaryId
    private Long counselingSummaryId;

    @Column(name = "SIMULATION_RESULT_ID")
    private Long simulationResultId;

    @Lob
    @Column(name = "ANALYSIS_COMMENT")
    private String analysisComment;

    @Lob
    @Column(name = "INDIVIDUAL_RESULTS_JSON")
    private String individualResultsJson; // 개별 분석 결과를 JSON 문자열로 저장

    @Column(name = "ANALYSIS_COMPLETED")
    private Integer analysisCompleted; // 최종 분석 완료 여부 추가

    @Column(name = "DOMINANT_EMOTION", length = 50)
    private String dominantEmotion; // 주요 감정

    // ✨ 새로 추가된 필드들 ✨
    @Column(name = "STRESS_SCORE")
    private Integer stressScore; // 스트레스 검사 점수

    @Column(name = "DEPRESSION_SCORE")
    private Integer depressionScore; // 우울증 검사 점수

    // Constructor (Lombok @NoArgsConstructor, @AllArgsConstructor가 대체하므로 명시적 생성자는 삭제)
    // public UserAnalysisSummaryEntity() { /* ... */ } // 삭제 또는 주석 처리

    @PrePersist
    protected void onCreate() {
        this.lastUpdated = new Date();
        if (this.analysisCompleted == null) {
            this.analysisCompleted = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = new Date();
    }

    // Getters and Setters (Lombok @Data가 대체하므로 모두 삭제)
    // public Long getUserSummaryId() { ... }
    // public void setUserSummaryId(Long userSummaryId) { ... }
    // ... 모든 Getter/Setter 메소드를 삭제합니다.
}