package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data                   // ✨ Getter, Setter, ToString 등을 포함
@Builder                // ✨ 객체 생성을 위한 빌더 패턴
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_PERSONALITY_ANALYSIS")
public class PersonalityTestResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personality_result_generator")
    @SequenceGenerator(
            name = "personality_result_generator",      // ✨ 2. 제너레이터 이름 설정 (위와 동일하게)
            sequenceName = "TB_PERSONALITY_ANALYSIS_SEQ", // ✨ 3. DB에 만든 시퀀스 이름
            allocationSize = 1                          // ✨ 4. 한 번에 1씩 증가
    )
    private Long personalityId;
    private String userId;
    private Long personalityTestId; // ✨ 이 필드를 추가해야 합니다.
    private String result;
    private String description;
    private String mbtiTitle;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}