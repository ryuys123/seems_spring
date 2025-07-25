package com.test.seems.test.jpa.entity;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_RESULTS")
public class PsychologicalScaleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESULT_ID")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, referencedColumnName = "USER_ID")
    private UserEntity user;

    @Column(name = "TEST_CATEGORY", nullable = false)
    private String testCategory;

    @Column(name = "TOTAL_SCORE", nullable = false)
    private Double totalScore;

    @Lob
    @Column(name = "INTERPRETATION")
    private String interpretation;

    @Column(name = "RISK_LEVEL")
    private String riskLevel;

    @Lob
    @Column(name = "SUGGESTIONS")
    private String suggestions;

    @Column(name = "CREATED_AT", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private Timestamp createdAt;
    
    @Column(name = "TEST_TYPE")
    private String testType;

    // Getters and Setters

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getTestCategory() {
        return testCategory;
    }

    public void setTestCategory(String testCategory) {
        this.testCategory = testCategory;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public com.test.seems.test.model.dto.PsychologicalTestResultResponse toDto() {
        return com.test.seems.test.model.dto.PsychologicalTestResultResponse.builder()
                .resultId(this.resultId)
                .userId(this.user.getUserId())
                .testType(this.testType)
                .diagnosisCategory(this.testCategory)
                .totalScore(this.totalScore)
                .interpretationText(this.interpretation)
                .riskLevel(this.riskLevel)
                .suggestions(this.suggestions)
                .testDateTime(this.createdAt.toLocalDateTime())
                .build();
    }
}
