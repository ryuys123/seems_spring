package com.test.seems.analysis.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TB_USER_ANALYSIS_SUMMARY")
public class UserAnalysisSummaryEntity {

    @Id
    @Column(name = "USER_SUMMARY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_summary_seq")
    @SequenceGenerator(name = "user_summary_seq", sequenceName = "SEQ_USER_ANALYSIS_SUMMARY_USER_SUMMARY_ID", allocationSize = 1)
    private Long userSummaryId;

    @Column(name = "USER_ID", length = 255)
    private String userId;

    @Column(name = "LAST_UPDATED")
    @Temporal(TemporalType.DATE)
    private Date lastUpdated;

    @Column(name = "PSYCHO_IMAGE_RESULT_ID")
    private Long psychoImageResultId;

    @Column(name = "PERSONALITY_RESULT_ID")
    private Long personalityResultId;

    @Column(name = "PSYCHO_SCALE_RESULT_ID")
    private Long psychoScaleResultId;

    @Column(name = "EMOTION_LOG_ID")
    private Long emotionLogId;

    @Column(name = "SUMMARY_ID") // CounselingAnalysisSummaryEntity's summaryId
    private Long counselingSummaryId;

    @Column(name = "SIMULATION_RESULT_ID")
    private Long simulationResultId;

    @Lob
    @Column(name = "ANALYSIS_COMMENT")
    private String analysisComment;

    @Lob
    @Column(name = "INDIVIDUAL_RESULTS_JSON")
    private String individualResultsJson; // 개별 분석 결과를 JSON 문자열로 저장

    @Column(name = "ANALYSIS_COMPLETED")
    private Integer analysisCompleted; // 최종 분석 완료 여부 추가

    // Constructors
    public UserAnalysisSummaryEntity() {
        this.lastUpdated = new Date();
        this.analysisCompleted = 0; // 초기값 0으로 설정
    }

    @PrePersist
    protected void onCreate() {
        this.lastUpdated = new Date();
        if (this.analysisCompleted == null) {
            this.analysisCompleted = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = new Date();
    }

    // Getters and Setters
    public Long getUserSummaryId() {
        return userSummaryId;
    }

    public void setUserSummaryId(Long userSummaryId) {
        this.userSummaryId = userSummaryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getPsychoImageResultId() {
        return psychoImageResultId;
    }

    public void setPsychoImageResultId(Long psychoImageResultId) {
        this.psychoImageResultId = psychoImageResultId;
    }

    public Long getPersonalityResultId() {
        return personalityResultId;
    }

    public void setPersonalityResultId(Long personalityResultId) {
        this.personalityResultId = personalityResultId;
    }

    public Long getPsychoScaleResultId() {
        return psychoScaleResultId;
    }

    public void setPsychoScaleResultId(Long psychoScaleResultId) {
        this.psychoScaleResultId = psychoScaleResultId;
    }

    public Long getEmotionLogId() {
        return emotionLogId;
    }

    public void setEmotionLogId(Long emotionLogId) {
        this.emotionLogId = emotionLogId;
    }

    public Long getCounselingSummaryId() {
        return counselingSummaryId;
    }

    public void setCounselingSummaryId(Long counselingSummaryId) {
        this.counselingSummaryId = counselingSummaryId;
    }

    public Long getSimulationResultId() {
        return simulationResultId;
    }

    public void setSimulationResultId(Long simulationResultId) {
        this.simulationResultId = simulationResultId;
    }

    public String getAnalysisComment() {
        return analysisComment;
    }

    public void setAnalysisComment(String analysisComment) {
        this.analysisComment = analysisComment;
    }

    public String getIndividualResultsJson() {
        return individualResultsJson;
    }

    public void setIndividualResultsJson(String individualResultsJson) {
        this.individualResultsJson = individualResultsJson;
    }

    public Integer getAnalysisCompleted() {
        return analysisCompleted;
    }

    public void setAnalysisCompleted(Integer analysisCompleted) {
        this.analysisCompleted = analysisCompleted;
    }
}