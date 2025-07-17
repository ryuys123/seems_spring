// src/main/java/com/test/seems/test/model/dto/PersonalityAnswerRequest.java (파일 이름 변경)
package com.test.seems.test.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// import com.test.seems.test.jpa.entity.PersonalityEntity; // 엔티티 임포트, 필요한 경우

@Data
@NoArgsConstructor
@AllArgsConstructor
// @Builder // 필요하다면 Builder 어노테이션 추가
public class PersonalityAnswerRequest { // 클래스 이름 변경
    private String userId;
    private Long questionId;
    private String answerValue; // ⭐ 성격 검사의 ANSWER_VALUE가 VARCHAR2(50)이므로 String으로 변경 고려
    private Long personalityTestId; // 이 답변이 어떤 특정 성격 테스트에 속하는지 식별

    // DTO를 엔티티로 변환하는 메소드 (실제 PersonalityEntity의 구조에 맞게 수정)
    // public PersonalityEntity toEntity() {
    //     return PersonalityEntity.builder()
    //             .userId(this.userId)
    //             .questionId(this.questionId)
    //             .answerValue(this.answerValue) // String 그대로
    //             .personalityTestId(this.personalityTestId) // PersonalityEntity에 이 필드가 있다면
    //             .build();
    // }
}