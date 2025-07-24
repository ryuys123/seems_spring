package com.test.seems.test.model.dto;

import java.util.List;

public class PersonalitySubmissionDto {
    private String userId;
    private List<PersonalityAnswerDto> answers;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PersonalityAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<PersonalityAnswerDto> answers) {
        this.answers = answers;
    }
}
