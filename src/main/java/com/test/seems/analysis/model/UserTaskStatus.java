package com.test.seems.analysis.model;

import lombok.Data;

@Data
public class UserTaskStatus {
    private String userId;
    private int counselingCompleted; // 0: 미완료, 1: 완료
    private int personalityTestCompleted; // 0: 미완료, 1: 완료
    private int psychImageTestCompleted; // 0: 미완료, 1: 완료
    private int depressionTestCompleted; // 0: 미완료, 1: 완료
    private int stressTestCompleted; // 0: 미완료, 1: 완료
    private int analysisCompleted; // 0: 미완료, 1: 완료

    public UserTaskStatus(String userId) {
        this.userId = userId;
        this.counselingCompleted = 0;
        this.personalityTestCompleted = 0;
        this.psychImageTestCompleted = 0;
        this.depressionTestCompleted = 0;
        this.stressTestCompleted = 0;
        this.analysisCompleted = 0;
    }

    public UserTaskStatus() {
        // 기본 생성자
    }
}