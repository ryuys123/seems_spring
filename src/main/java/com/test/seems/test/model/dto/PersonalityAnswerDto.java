package com.test.seems.test.model.dto;

public class PersonalityAnswerDto {
    private Long questionId;
    private int answerValue; // e.g., 1-5 or a specific value representing the choice
    private String scoreDirection; // "E", "I", "S", "N", "T", "F", "J", "P"

    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public int getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(int answerValue) {
        this.answerValue = answerValue;
    }

    public String getScoreDirection() {
        return scoreDirection;
    }

    public void setScoreDirection(String scoreDirection) {
        this.scoreDirection = scoreDirection;
    }
}
