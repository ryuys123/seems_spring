package com.test.seems.test.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologicalTestResultResponse {
    private Long resultId;
    private String userId;
    private Long questionId;        // 이미지 검사 또는 AI 텍스트 분석 시 대표 문항 ID (척도 검사 시 null 가능)
    private String rawResponseText; // 이미지 검사 또는 AI 텍스트 분석 시 원본 텍스트 (척도 검사 시 null 가능)
    private String testType;
    // ⭐⭐ 새로 추가할 필드: 척도 검사 결과에 특화된 정보 (TB_PSYCHOLOGICAL_SCALE_RESULTS에 매핑될 필드) ⭐⭐
    private Integer totalScore;        // 척도 검사 (우울증/스트레스)의 총점
    private String diagnosisCategory;  // 척도 검사 (우울증/스트레스)의 진단 카테고리 (예: "정상", "경도 우울")
    private String interpretationText; // 척도 검사 (우울증/스트레스)의 결과 해석 및 조언
    private String riskLevel;          // 척도 검사 (우울증/스트레스)의 위험도 수준 (예: "NORMAL", "CAUTION", "HIGH_RISK")

    // AI 분석 관련 필드 (주로 이미지 검사나 자유 텍스트 분석에 사용)
    private String aiSentiment;
    private Double aiSentimentScore;
    private Double aiCreativityScore;
    private String aiPerspectiveKeywords;
    private String aiInsightSummary; // AI가 제공하는 심층 분석 요약 (이미지/자유 텍스트용)
    private String suggestions; // 사용자에게 줄 제안 (모든 검사 유형에 공통적으로 사용 가능)

    private LocalDateTime testDateTime; // 검사 수행 시간 (생성 시간)
}