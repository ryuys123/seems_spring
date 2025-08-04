package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalitySubmission {
    private String userId;
    private List<PersonalityAnswer> answers;
}