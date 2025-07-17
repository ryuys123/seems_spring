package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "TB_PERSONALITY_ANSWERS") // ⭐ 테이블명 변경됨
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personality_answers_seq_gen") // ⭐ 제너레이터 이름 변경
    @SequenceGenerator(name = "personality_answers_seq_gen", sequenceName = "SEQ_PERSONALITY_ANSWERS_AID", allocationSize = 1) // ⭐ 시퀀스 이름 변경
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    // ⭐ ANSWER_VALUE 컬럼이 DDL에서 VARCHAR2(50)이므로 String으로 변경합니다. ⭐
    @Column(name = "ANSWER_VALUE", nullable = false, length = 50)
    private String answerValue; // MBTI 답변은 숫자보다 문자열이므로 String으로 매핑
}