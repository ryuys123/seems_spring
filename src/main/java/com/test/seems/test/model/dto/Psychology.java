package com.test.seems.test.model.dto;

import lombok.Data; // <<-- @Data 사용
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Psychology {
    private Long userId;
    private Long questionId;
    private Integer answerValue;
}
