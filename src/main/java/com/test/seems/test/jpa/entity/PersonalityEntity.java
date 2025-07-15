package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "TB_PERSONALITY_TEST_ANSWERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle은 GenerationType.SEQUENCE 고려
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId; // FK

    @Column(name = "ANSWER_VALUE", nullable = false)
    private Integer answerValue; // 1~5점 척도 등


}