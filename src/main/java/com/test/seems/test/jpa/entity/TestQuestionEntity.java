package com.test.seems.test.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import com.test.seems.test.model.dto.TestQuestion; // <<-- DTO 임포트


@Entity
@Table(name = "TB_TEST_QUESTIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "TEST_TYPE", nullable = false, length = 50)
    private String testType;

    @Column(name = "QUESTION_TEXT", nullable = false, length = 255)
    private String questionText;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @Column(name = "SCORE_DIRECTION", length = 10)
    private String scoreDirection;

    // 엔티티를 DTO로 변환하는 메소드
    public TestQuestion toDto() { // <<-- 이 메소드 추가
        return TestQuestion.builder()
                .questionId(this.questionId)
                .questionText(this.questionText)
                .category(this.category)
                .weight(this.weight)
                .scoreDirection(this.scoreDirection)
                .build();
    }
}