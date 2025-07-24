package com.test.seems.test.jpa.entity;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_ANSWERS")
public class PsychologicalScaleAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ANSWER_ID")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "ANSWER_VALUE", nullable = false)
    private int answerValue;

    @Column(name = "TEST_CATEGORY", nullable = false)
    private String testCategory;

    @Column(name = "ANSWER_DATETIME", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private Timestamp answerDatetime;
    
    @Column(name = "TEST_TYPE")
    private String testType;

    // Getters and Setters

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

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

    public String getTestCategory() {
        return testCategory;
    }

    public void setTestCategory(String testCategory) {
        this.testCategory = testCategory;
    }

    public Timestamp getAnswerDatetime() {
        return answerDatetime;
    }

    public void setAnswerDatetime(Timestamp answerDatetime) {
        this.answerDatetime = answerDatetime;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
