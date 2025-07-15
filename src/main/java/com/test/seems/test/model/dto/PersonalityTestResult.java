package com.test.seems.test.model.dto;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonalityTestResult {

    // 이 DTO가 가질 데이터 필드들
    private String result;
    private String description;
    private String mbtiTitle;

    /**
     * DTO 자신을 Entity 객체로 변환하는 메서드.
     * Service로부터 추가 정보를 받아와 Entity를 생성합니다.
     */
    public PersonalityTestResultEntity toEntity(String userId, Long personalityTestId) {
        return PersonalityTestResultEntity.builder()
                .userId(userId)
                .personalityTestId(personalityTestId) // 임시값 (예: 1L)
                .result(this.result) // this는 DTO 자기 자신을 가리킴
                .description(this.description)
                .mbtiTitle(this.mbtiTitle)
                .build();
    }
}