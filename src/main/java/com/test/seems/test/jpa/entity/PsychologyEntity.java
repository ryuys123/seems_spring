package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_TEST_ANSWERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsychologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle은 GenerationType.SEQUENCE 고려
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId; // 답변을 한 사용자 ID (USER 테이블의 FK)

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId; // 답변한 문항 ID (TB_TEST_QUESTIONS의 FK)

    @Column(name = "ANSWER_VALUE", nullable = false, length = 1000) // 사용자의 느낀 점 텍스트 (CLOB 대신 VARCHAR2(1000) 예시)
    private String answerValue; // CLOB 사용 시 @Lob 어노테이션 추가 가능 (DBMS에 따라 VARCHAR2 최대 길이 확인)

    @CreatedDate
    @Column(name = "ANSWER_DATETIME", nullable = false, updatable = false)
    private LocalDateTime answerDatetime; // 답변이 저장된 시각
}
