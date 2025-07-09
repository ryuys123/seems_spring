package com.test.seems.test.controller;

// 임포트 경로 수정: model.dto 바로 아래에 TestQuestion.java 가 있다고 가정

import com.test.seems.test.model.dto.Personality;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personality-test")
public class PersonalityController {

    private final PersonalityService personalityService;

    @Autowired
    public PersonalityController(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }

    /**
     * 역할: 성격 검사 문항 목록을 프론트엔드에 제공합니다.
     * 반환 타입: ResponseEntity<List<TestQuestion>>
     */
    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getPersonalityQuestions() { // <<-- 반환 타입 TestQuestion으로 변경
        List<TestQuestion> questions = personalityService.getPersonalityQuestions();
        return ResponseEntity.ok(questions);
    }

    /**
     * 역할: 사용자가 성격 검사 답변을 제출하면, 이를 저장하고 MBTI 결과를 계산합니다.
     * 요청 본문: List<Personality>
     * 반환 타입: ResponseEntity<String>
     */
    @PostMapping("/submit-answers")
    public ResponseEntity<String> submitPersonalityAnswers(
            @RequestBody List<Personality> answers // <<-- Personality로 변경
    ) {
        personalityService.submitAnswersAndCalculateResult(answers);
        return ResponseEntity.ok("성격 검사 답변이 성공적으로 제출되었습니다.");
    }

    // 결과 조회 API는 현재 제외

    /*
    // /**
    //  * 역할: 특정 사용자의 MBTI 검사 결과를 조회합니다. (현재는 제외)
    //  * HTTP 메서드: GET
    //  * 엔드포인트: /api/personality-test/results/{userId}
    //  * 경로 변수: userId (조회하고자 하는 사용자 ID)
    //  * 반환 타입: ResponseEntity<PersonalityTestResultResponseDto>
    //  */
    // @GetMapping("/results/{userId}")
    // public ResponseEntity<PersonalityTestResultResponseDto> getPersonalityTestResult(@PathVariable Long userId) {
    //     // 이 메서드는 나중에 결과 페이지 구현 시 다시 추가할 예정
    //     return ResponseEntity.notFound().build();
    // }

}