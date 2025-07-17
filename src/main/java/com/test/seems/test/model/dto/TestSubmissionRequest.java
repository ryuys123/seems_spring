// src/main/java/com/test/seems/test/model/dto/DepressionTestSubmissionRequest.java
package com.test.seems.test.model.dto;

import lombok.*;

import java.util.List;
// 여러 개의 PsychologicalScaleAnswerRequest DTO를 리스트(answers) 형태로 묶어서
// 백엔드로 한 번에 전송할 때 사용하는 래퍼(wrapper) DTO입니다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionRequest {
    private List<PsychologicalScaleAnswerRequest> answers; // 핵심! 개별 답변 DTO의 리스트
    // 필요하다면 여기에 scaleType (예: "DEPRESSION_SCALE") 등 전체 검사 정보를 추가할 수 있습니다.
    // private String scaleType;
}