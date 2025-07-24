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
    private Long emotionLogId;
    private Long counselingSummaryId;
    private Long simulationResultId;
    private String analysisComment;
    private Integer analysisCompleted;
    private List<Map<String, Object>> individualResults; // Python AI 서버에서 받은 개별 결과
}
