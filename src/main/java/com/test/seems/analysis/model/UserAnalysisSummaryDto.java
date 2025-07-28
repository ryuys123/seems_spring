package com.test.seems.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnalysisSummaryDto {
    private Long userSummaryId;
    private String userId;
    private Date lastUpdated;
    private Long psychoImageResultId;
    private Long personalityResultId;
    private Long psychoScaleResultId;
    private Long emotionId;
    private Long counselingSummaryId;
    private Long simulationResultId;
    private String analysisComment;
    private Integer analysisCompleted;
    private List<Map<String, Object>> individualResults; // Python AI 서버에서 받은 개별 결과
    private String dominantEmotion; // AI 종합 분석의 주요 감정

    // ✨ 새로 추가할 필드들 ✨
    private Integer stressScore; // 스트레스 검사 점수
    private Integer depressionScore; // 우울증 검사 점수
}
