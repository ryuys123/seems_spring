package com.test.seems.test.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
// : mbtiTitle, result, description 등을 포함하고 PersonalityTestResultEntity로 변환하는 것을 보아
// MBTI와 같은 특정 '성격 테스트'의 최종 결과를 나타내는 DTO입니다
@Data
@AllArgsConstructor
public class PersonalityTestResult {

    // 이 DTO가 가질 데이터 필드들
    private String result;
    private String description;
    private String mbtiTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
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