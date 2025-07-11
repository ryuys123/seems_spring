package com.test.seems.test.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Lombok @Builder
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.test.seems.test.model.dto.PsychologicalTestResultResponse; // 변환할 DTO 임포트

@Entity // 이 클래스가 JPA 엔티티임을 선언
@Table(name = "TB_PSYCHOLOGICAL_ANALYSIS") // 매핑될 실제 DB 테이블명 지정
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
@Data // Getter, Setter 등 포함
@NoArgsConstructor
@AllArgsConstructor
@Builder // Lombok @Builder
public class PsychologicalTestResultEntity {

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_seq")
    @SequenceGenerator(name = "result_seq", sequenceName = "RESULT_SEQ", allocationSize = 1)
    @Column(name = "RESULT_ID", nullable = false)
    private Long resultId;

    @Column(name = "USER_ID", nullable = false, length = 255) // TB_USERS.USER_ID 와 타입 일치
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false) // TB_TEST_QUESTIONS.QUESTION_ID 참조 (어떤 이미지에 대한 분석인지)
    private Long questionId;

    @Lob // <<-- CLOB 타입 매핑 (긴 텍스트)
    @Column(name = "RAW_RESPONSE_TEXT", nullable = false)
    private String rawResponseText; // 사용자가 작성한 원본 느낀 점 텍스트

    @Column(name = "AI_SENTIMENT", length = 50)
    private String aiSentiment; // AI 분석 주 감정

    @Column(name = "AI_SENTIMENT_SCORE") // NUMBER(5,2)에 매핑
    private Double aiSentimentScore; // AI 분석 감정 점수

    @Column(name = "AI_CREATIVITY_SCORE") // NUMBER(5,2)에 매핑
    private Double aiCreativityScore; // AI 분석 창의력/상상력 지표 점수

    @Column(name = "AI_PERSPECTIVE_KEYWORDS", length = 500) // VARCHAR2(500)
    private String aiPerspectiveKeywords; // AI 분석 관점 키워드 (쉼표로 구분)

    @Lob // <<-- CLOB 타입 매핑 (긴 텍스트)
    @Column(name = "AI_INSIGHT_SUMMARY")
    private String aiInsightSummary; // AI가 제공하는 심층 분석 요약 및 통찰

    @Column(name = "SUGGESTIONS", length = 2000) // VARCHAR2(2000)
    private String suggestions; // 맞춤형 제안 / 조언

    @CreatedDate // 생성 시 자동으로 현재 시각 기록
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 분석 생성 시간

    // 엔티티를 DTO로 변환하는 메소드 (서비스 계층에서 호출)
    public PsychologicalTestResultResponse toDto() {
        return PsychologicalTestResultResponse.builder()
                .resultId(this.resultId)
                .userId(this.userId)
                .questionId(this.questionId)
                .rawResponseText(this.rawResponseText)
                .aiSentiment(this.aiSentiment)
                .aiSentimentScore(this.aiSentimentScore)
                .aiCreativityScore(this.aiCreativityScore)
                .aiPerspectiveKeywords(this.aiPerspectiveKeywords)
                .aiInsightSummary(this.aiInsightSummary)
                .suggestions(this.suggestions)// createdAt을 testDateTime에 매핑
                .build();
    }
}