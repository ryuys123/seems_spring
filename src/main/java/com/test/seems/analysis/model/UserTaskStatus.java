package com.test.seems.analysis.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskStatus {
    private String userId;
    private Integer counselingCompleted;
    private Integer personalityTestCompleted;
    private Integer psychImageTestCompleted;
    private Integer depressionTestCompleted;
    private Integer stressTestCompleted;
    private Integer analysisCompleted; // 최종 분석 완료 여부 추가

    public UserTaskStatus(String userId) {
        this.userId = userId;
        this.counselingCompleted = 0;
        this.personalityTestCompleted = 0;
        this.psychImageTestCompleted = 0;
        this.depressionTestCompleted = 0;
        this.stressTestCompleted = 0;
        this.analysisCompleted = 0; // 초기값 0으로 설정
    }
}