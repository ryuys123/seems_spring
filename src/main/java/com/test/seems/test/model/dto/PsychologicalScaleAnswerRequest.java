package com.test.seems.test.model.dto; // <<-- dto 바로 밑에 위치

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.test.seems.test.jpa.entity.PsychologyEntity; // 엔티티 임포트 (답변 엔티티로 변환)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsychologicalScaleAnswerRequest {
    private String userId;      // 사용자 ID
    private Long questionId;    // 답변한 문항 ID
    private Integer answerValue; // <<-- 사용자의 숫자 답변 값 (예: 1, 2, 3, 4, 5)
// 우울증, 스트레스와 같이 점수를 선택하는 척도 기반 심리 검사의 각 개별 문항에 대한
// 사용자의 답변(answerValue) 을 백엔드로 전송할 때 사용하는 DTO입니다.
private String testType;
    // DTO를 엔티티로 변환하는 메소드
    public PsychologyEntity toEntity() {
        // userResponseText가 아닌 answerValue를 사용하여 PsychologyEntity를 생성합니다.
        // PsychologyEntity의 userResponseText 필드는 String이므로, Integer를 String으로 변환합니다.
        return PsychologyEntity.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .userResponseText(String.valueOf(this.answerValue)) // <<-- Integer를 String으로 변환
                .build();
    }
}