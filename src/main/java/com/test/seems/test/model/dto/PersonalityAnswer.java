package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityAnswer {
    private Long questionId;
    private int answerValue; // e.g., 1-5
    private String scoreDirection; // "E", "I", "S", "N", "T", "F", "J", "P"
}