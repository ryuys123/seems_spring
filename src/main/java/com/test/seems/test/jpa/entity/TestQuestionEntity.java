package com.test.seems.test.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import com.test.seems.test.model.dto.TestQuestion;

@Entity
@Table(name = "TB_COMMON_QUESTIONS") // ⭐ 테이블명 변경됨
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "common_questions_seq_gen") // ⭐ 제너레이터 이름 변경
    @SequenceGenerator(name = "common_questions_seq_gen", sequenceName = "SEQ_COMMON_QUESTIONS_QID", allocationSize = 1) // ⭐ 시퀀스 이름 변경
    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "TEST_TYPE", nullable = false, length = 50)
    private String testType;

    @Column(name = "QUESTION_TEXT", nullable = false, length = 255)
    private String questionText;

    @Column(name = "IMAGE_URL", length = 255)
    private String imageUrl;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @Column(name = "SCORE_DIRECTION", length = 10)
    private String scoreDirection;

    public TestQuestion toDto() {
        return TestQuestion.builder()
                .questionId(this.questionId)
                .questionText(this.questionText)
                .imageUrl(this.imageUrl)
                .category(this.category)
                .testType(this.testType)
                .weight(this.weight)
                .scoreDirection(this.scoreDirection)
                .build();
    }
}