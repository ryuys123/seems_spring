package com.test.seems.test.jpa.entity;

import com.test.seems.test.model.dto.PsychologicalAnswerRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_IMAGE_ANSWERS") // ⭐ 테이블명 변경됨
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologyEntity { // 엔티티 이름은 유지
    @PrePersist
    protected void onCreate() {
        if (answerDatetime == null) {
            answerDatetime = LocalDateTime.now();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psych_image_answers_seq_gen") // ⭐ 제너레이터 이름 변경
    @SequenceGenerator(name = "psych_image_answers_seq_gen", sequenceName = "SEQ_PSYCH_IMAGE_ANSWERS_AID", allocationSize = 1) // ⭐ 시퀀스 이름 변경
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Lob
    @Column(name = "USER_RESPONSE_TEXT", nullable = false)
    private String userResponseText;

    @Column(name = "TEST_TYPE", nullable = false) // DB 컬럼과 매핑
    private String testType; // ✨ 이 필드를 클래스에 추가해주세요!

    @CreatedDate
    @Column(name = "ANSWER_DATETIME", nullable = false, updatable = false)
    private LocalDateTime answerDatetime;

    public PsychologicalAnswerRequest toDto() {
        return PsychologicalAnswerRequest.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .userResponseText(this.userResponseText)
                .testType(this.testType)
                // PsychologicalAnswerRequest의 currentStep, totalSteps는 요청 시에만 사용되므로 DTO 변환 시 포함하지 않음
                .build();
    }
}