package com.test.seems.test.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import com.test.seems.test.model.dto.TestQuestion; // <<-- DTO 임포트 (Dto 접미사 없음)

@Entity
@Table(name = "TB_TEST_QUESTIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle은 GenerationType.SEQUENCE 고려
    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "TEST_TYPE", nullable = false, length = 50)
    private String testType;

    @Column(name = "QUESTION_TEXT", nullable = false, length = 255) // <<-- NOT NULL 유지
    private String questionText; // 사용자에게 제시할 '질문 텍스트'

    @Column(name = "IMAGE_URL", length = 255) // <<-- IMAGE_URL 컬럼 추가 (NULL 허용)
    private String imageUrl; // 이미지 파일의 URL 또는 경로

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @Column(name = "SCORE_DIRECTION", length = 10)
    private String scoreDirection;

    // 엔티티를 DTO로 변환하는 메소드
    public TestQuestion toDto() { // <<-- TestQuestion DTO 반환
        return TestQuestion.builder() // <<-- TestQuestion.builder() 사용
                .questionId(this.questionId)
                .questionText(this.questionText)
                .imageUrl(this.imageUrl)
                .category(this.category)
                .weight(this.weight)
                .scoreDirection(this.scoreDirection)
                .build();
    }
}