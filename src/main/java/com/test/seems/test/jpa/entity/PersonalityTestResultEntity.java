package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // @CreationTimestamp 사용 시 필요

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_PERSONALITY_RESULTS") // ⭐ 테이블명 변경됨
public class PersonalityTestResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personality_results_seq_gen") // ⭐ 제너레이터 이름 변경
    @SequenceGenerator(
            name = "personality_results_seq_gen",      // ⭐ 제너레이터 이름 설정
            sequenceName = "SEQ_PERSONALITY_ANALYSIS_PERSONALITY_ID", // ⭐ DB에 만든 새로운 시퀀스 이름
            allocationSize = 1
    )
    @Column(name = "PERSONALITY_ID") // 컬럼명 명시 (DB 컬럼명과 일치)
    private Long personalityId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PERSONALITY_TEST_ID") // TB_PERSONALITY_RESULTS DDL에 이 필드가 있다면 유지
    private Long personalityTestId;

    @Column(name = "RESULT")
    private String result;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MBTI_TITLE")
    private String mbtiTitle;

    @CreationTimestamp // 엔티티가 처음 저장될 때 자동으로 시간 기록
    @Column(name = "CREATED_AT", updatable = false) // 한번 기록되면 업데이트 불가
    private LocalDateTime createdAt;
}