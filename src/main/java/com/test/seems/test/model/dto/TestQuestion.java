package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <<-- @Builder 임포트 추가

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // <<-- 이 어노테이션을 추가합니다!
public class TestQuestion {
    private Long questionId;
    private String questionText;
    private String category;
    private Double weight;
    private String scoreDirection;
}