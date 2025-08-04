package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleAnswer {
    private Long questionId;
    private int answerValue;
}