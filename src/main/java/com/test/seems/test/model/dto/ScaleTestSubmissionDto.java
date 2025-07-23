package com.test.seems.test.model.dto;

import java.util.List;

public class ScaleTestSubmissionDto {
    private String userId;
    private String testCategory; // e.g., "DEPRESSION_SCALE", "STRESS_SCALE"
    private List<ScaleAnswerDto> answers;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTestCategory() {
        return testCategory;
    }

    public void setTestCategory(String testCategory) {
        this.testCategory = testCategory;
    }

    public List<ScaleAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ScaleAnswerDto> answers) {
        this.answers = answers;
    }
}
