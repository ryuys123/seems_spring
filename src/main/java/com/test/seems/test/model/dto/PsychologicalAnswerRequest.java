package com.test.seems.test.model.dto; // <<-- 패키지 경로 수정 (dto 바로 밑)

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.test.seems.test.jpa.entity.PsychologyEntity; // 엔티티 임포트 확인

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologicalAnswerRequest {
    private String userId;
    private Long questionId;
    private String userResponseText;

    public PsychologyEntity toEntity() {
        return PsychologyEntity.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .userResponseText(this.userResponseText)
                .build();
    }
}