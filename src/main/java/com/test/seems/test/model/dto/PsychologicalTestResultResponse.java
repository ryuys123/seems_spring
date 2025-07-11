package com.test.seems.test.model.dto; // <<-- 패키지 경로 확인

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <<-- @Builder 임포트 확인
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // <<-- @Builder 어노테이션이 반드시 있어야 합니다.
public class PsychologicalTestResultResponse { // <<-- 클래스 이름 확인
    private Long resultId;
    private String userId;
    private Long questionId;
    private String rawResponseText;

    private String aiSentiment;
    private Double aiSentimentScore;
    private Double aiCreativityScore;
    private String aiPerspectiveKeywords;
    private String aiInsightSummary;
    private String suggestions;
    private LocalDateTime testDateTime; // <<-- 이 필드의 철자가 'testDateTime'으로 정확한지 확인

    // 이 DTO는 주로 엔티티에서 toDto()로 변환될 때 사용됩니다.
    // 만약 이 DTO 내에 toEntity() 메소드가 있다면 (흔치는 않지만), Builder를 사용하여 구현할 수 있습니다.
    /*
    public PsychologicalTestResultEntity toEntity() {
        return PsychologicalTestResultEntity.builder()
                .resultId(this.resultId)
                // ... 나머지 필드
                .build();
    }
    */
}