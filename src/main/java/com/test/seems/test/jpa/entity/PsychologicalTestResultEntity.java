package com.test.seems.test.jpa.entity;

import com.test.seems.test.model.dto.PsychologicalTestResultResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_IMAGE_RESULTS") // ⭐ 테이블명 변경됨
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologicalTestResultEntity {

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psych_image_results_seq_gen") // ⭐ 제너레이터 이름 변경
    @SequenceGenerator(name = "psych_image_results_seq_gen", sequenceName = "SEQ_PSYCH_IMAGE_RESULTS_RID", allocationSize = 1) // ⭐ 시퀀스 이름 변경
    @Column(name = "RESULT_ID", nullable = false)
    private Long resultId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false) // 어떤 이미지에 대한 분석인지
    private Long questionId;

    @Lob
    @Column(name = "RAW_RESPONSE_TEXT", nullable = false)
    private String rawResponseText;

    // ⭐⭐ 새로 추가할 필드: 어떤 종류의 검사 결과인지 명시 ⭐⭐
    @Column(name = "TEST_TYPE", length = 50) // TB_PSYCHOLOGICAL_IMAGE_RESULTS DDL에 이 컬럼 추가 필수
    private String testType; // 예: "IMAGE_TEST"

    @Column(name = "AI_SENTIMENT", length = 50)
    private String aiSentiment;

    @Column(name = "AI_SENTIMENT_SCORE")
    private Double aiSentimentScore;

    @Column(name = "AI_CREATIVITY_SCORE")
    private Double aiCreativityScore;

    @Column(name = "AI_PERSPECTIVE_KEYWORDS", length = 500)
    private String aiPerspectiveKeywords;

    @Lob
    @Column(name = "AI_INSIGHT_SUMMARY")
    private String aiInsightSummary;

    @Column(name = "SUGGESTIONS", length = 2000)
    private String suggestions;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PsychologicalTestResultResponse toDto() {
        return PsychologicalTestResultResponse.builder()
                .resultId(this.resultId)
                .userId(this.userId)
                .questionId(this.questionId)
                .rawResponseText(this.rawResponseText)
                .testType(this.testType)
                .aiSentiment(this.aiSentiment)
                .aiSentimentScore(this.aiSentimentScore)
                .aiCreativityScore(this.aiCreativityScore)
                .aiPerspectiveKeywords(this.aiPerspectiveKeywords)
                .aiInsightSummary(this.aiInsightSummary)
                .suggestions(this.suggestions)
                .testDateTime(this.createdAt)
                .build();
    }
}