package com.test.seems.test.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // toDto() 메소드에서 Builder 사용 시 필요
import org.springframework.data.annotation.CreatedDate; // 생성 시 날짜 자동 기록
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Auditing 기능 리스너

import jakarta.persistence.*;
import java.time.LocalDateTime; // Java 8+ 날짜/시간 API
import com.test.seems.test.model.dto.PsychologicalAnswerRequest; // toDto()에 사용될 DTO 임포트

@Entity // 이 클래스가 JPA 엔티티임을 선언
@Table(name = "TB_PSYCHOLOGICAL_TEST_ANSWERS") // 매핑될 실제 DB 테이블명 지정
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
@Data // Getter, Setter 등 포함
@NoArgsConstructor
@AllArgsConstructor
@Builder // toDto() 메소드에서 사용 시 필요 (Lombok @Builder)
public class PsychologyEntity { // <<-- PsychologyEntity 이름 유지

    // PsychologyEntity.java에 추가
    @PrePersist
    protected void onCreate() {
        if (answerDatetime == null) {
            answerDatetime = LocalDateTime.now();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_seq")
    @SequenceGenerator(name = "answer_seq", sequenceName = "ANSWER_SEQ", allocationSize = 1)
    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "USER_ID", nullable = false, length = 255) // TB_USERS.USER_ID 와 타입 일치
    private String userId; // 답변을 한 사용자 ID

    @Column(name = "QUESTION_ID", nullable = false) // TB_TEST_QUESTIONS의 FK
    private Long questionId; // 답변한 문항 ID

    @Lob // <<-- CLOB 타입 매핑 (긴 텍스트 저장)
    @Column(name = "USER_RESPONSE_TEXT", nullable = false) // 컬럼명 및 NOT NULL 유지
    private String userResponseText; // 사용자가 작성한 느낀 점 텍스트

    @CreatedDate // 엔티티가 영속화(저장)될 때 현재 시각 자동 기록
    @Column(name = "ANSWER_DATETIME", nullable = false, updatable = false) // 한번 기록되면 업데이트 불가
    private LocalDateTime answerDatetime; // 답변이 저장된 시각

    // 엔티티를 DTO로 변환하는 메소드 (서비스 계층에서 호출)
    public PsychologicalAnswerRequest toDto() { // <<-- PsychologicalAnswerRequest DTO 반환
        return PsychologicalAnswerRequest.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .userResponseText(this.userResponseText)
                .build();
    }
}