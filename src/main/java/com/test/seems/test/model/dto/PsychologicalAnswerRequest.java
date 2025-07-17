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
    private int currentStep; // 현재 진행 단계 (예: 1, 2, 3)
    private int totalSteps;  // 전체 단계 수 (예: 3)
    private  String testType;
// : 이미지 기반 심리 검사처럼 사용자가 입력한
// 자유 텍스트 답변(userResponseText) 을 백엔드로 전송할 때 사용하는 DTO입니다.
    public PsychologyEntity toEntity() {
        return PsychologyEntity.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .userResponseText(this.userResponseText)
                .build();
    }
}