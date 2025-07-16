package com.test.seems.simulation.model.dto;

import com.test.seems.simulation.jpa.entity.SimulationQuestionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationQuestion {

    private Long questionId;
    private Long scenarioId;
    private Integer questionNumber;
    private String questionText;
    private List<Option> options; // 선택지 목록
    private Long settingId;
    // 선택지 DTO (내부 클래스)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Option {
        private String text;
        private String trait; // 성향 특성
        private String nextNarrative; // 다음 시나리오 설명
    }

    // 엔티티(SimulationQuestionEntity)에서 DTO로 변환하는 메서드
    public static SimulationQuestion fromEntity(SimulationQuestionEntity entity) {

        // 주의: entity.getChoiceOptions()는 JSON 문자열이므로,
        // JSON 파서를 사용하여 List<Option>으로 변환해야 합니다.
        // 예를 들어 Jackson 라이브러리의 ObjectMapper를 사용합니다.

        // List<Option> parsedOptions = parseOptionsFromJson(entity.getChoiceOptions());

        return SimulationQuestion.builder()
                .questionId(entity.getQuestionId())
                .scenarioId(entity.getScenarioId())
                .questionNumber(entity.getQuestionNumber())
                .questionText(entity.getQuestionText())
                // .options(parsedOptions) // 파싱된 옵션 리스트 설정
                .build();
    }
}