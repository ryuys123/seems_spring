// src/main/java/com/test/seems/test/model/entity/ScaleTestAnswerEntity.java
package com.test.seems.test.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_ANSWERS") // ⭐ 이 엔티티가 매핑될 테이블명
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScaleTestAnswerEntity {

    @PrePersist
    protected void onCreate() {
        if (answerDatetime == null) {
            answerDatetime = LocalDateTime.now();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psych_scale_answers_seq_gen") // ⭐ 시퀀스 제너레이터 설정
    @SequenceGenerator(name = "psych_scale_answers_seq_gen", sequenceName = "SEQ_PSYCH_SCALE_ANSWERS_AID", allocationSize = 1) // ⭐ 실제 시퀀스 이름
    @Column(name = "ANSWER_ID")
    private Long answerId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "ANSWER_VALUE", nullable = false)
    private Integer answerValue; // DDL의 NUMBER(5,0)에 매핑

    @Column(name = "TEST_CATEGORY", nullable = false) // "DEPRESSION_SCALE", "STRESS_SCALE"
    private String testCategory;

    @Column(name = "TEST_TYPE", nullable = false)
    private String testType; // ✨ 이 필드를 엔티티에 추가하세요!

    @CreatedDate
    @Column(name = "ANSWER_DATETIME", nullable = false, updatable = false)
    private LocalDateTime answerDatetime;
}