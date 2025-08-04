package com.test.seems.simulation.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized // Builder 패턴과 Jackson 직렬화/역직렬화 함께 사용 시 필요
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON으로 만들 때 null 값인 필드는 제외
public class SimulationQuestion {

    private Long settingId;
    private Long scenarioId;
    private Integer questionNumber;
    private String questionText;
    private Boolean isSimulationEnded;
    private List<ChoiceOption> options;


    /**
     * 개별 선택지 하나의 정보를 담는 static 내부 클래스입니다.
     * `trait` 필드가 다시 포함되고, `resultPersonalityType` 필드는 제거됩니다.
     */
    @Data
    @Builder
    @Jacksonized
    public static class ChoiceOption {
        private String text;
        private String trait; // ✅ 이 필드가 다시 포함됩니다.
        private Integer nextQuestionNumber;
    }


}