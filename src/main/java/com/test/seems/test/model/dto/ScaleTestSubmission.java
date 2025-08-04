package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleTestSubmission {
    private String userId;
    private String testCategory; // e.g., "DEPRESSION_SCALE", "STRESS_SCALE"
    private List<ScaleAnswer> answers;
}