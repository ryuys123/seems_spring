package com.test.seems.test.model.dto; // <<-- 패키지 경로 확인

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <<-- @Builder 임포트 확인

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // TestQuestionEntity.toDto()에서 사용하므로 필수
public class TestQuestion { // <<-- 이름이 TestQuestion입니다.
    private Long questionId;
    private String questionText;
    private String imageUrl; // <<-- imageUrl 필드 추가
    private String category;
    private Double weight;
    private String scoreDirection;
}