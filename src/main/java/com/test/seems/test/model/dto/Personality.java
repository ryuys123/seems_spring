package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.test.seems.test.jpa.entity.PersonalityEntity; // <<-- 엔티티 임포트


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personality {
    private String userId;
    private Long questionId;
    private Integer answerValue;

    // DTO를 엔티티로 변환하는 메소드
    public PersonalityEntity toEntity() { // <<-- 이 메소드 추가
        return PersonalityEntity.builder()
                .userId(this.userId)
                .questionId(this.questionId)
                .answerValue(this.answerValue)
                // answerId, answerDatetime은 DB나 Auditing에서 자동 생성되므로 여기서는 설정하지 않음
                .build();
    }
}