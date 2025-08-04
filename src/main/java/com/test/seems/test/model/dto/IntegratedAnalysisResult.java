package com.test.seems.test.model.dto;

import com.test.seems.counseling.model.dto.CounselingDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntegratedAnalysisResult {
    private PersonalityTestResult latestPersonalityResult;
    private PsychologicalTestResultResponse latestImageResult;
    private PsychologicalTestResultResponse latestDepressionResult;
    private PsychologicalTestResultResponse latestStressResult;
    private CounselingDto.DetailResponse latestCounselingSummary;
}
