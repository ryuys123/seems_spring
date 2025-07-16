package com.test.seems.test.controller;

import com.test.seems.test.model.dto.Personality;
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.service.PersonalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor // final 필드에 대한 생성자를 만들어주므로 @Autowired 생략 가능
@RequestMapping("/api/personality-test")
public class PersonalityController {

    private final PersonalityService personalityService;

    /**
     * 역할: 성격 검사 문항 목록을 프론트엔드에 제공합니다.
     */
    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getPersonalityQuestions() {
        List<TestQuestion> questions = personalityService.getPersonalityQuestions();
        return ResponseEntity.ok(questions);
    }

    /**
     * 역할: 사용자가 성격 검사 답변을 제출하면, 이를 저장하고 MBTI 결과를 계산하여 반환합니다.
     */
    @PostMapping("/submit-answers")
    // ✨ 1. 답변 제출 시에도 결과를 바로 반환하도록 수정
    public ResponseEntity<PersonalityTestResult> submitPersonalityAnswers(
            @RequestBody List<Personality> answers
    ) {
        // Service는 이제 계산된 결과 DTO를 반환합니다.
        PersonalityTestResult result = personalityService.submitAnswersAndCalculateResult(answers);
        // 클라이언트에게 성공(200 OK)과 함께 결과 데이터를 전달합니다.
        return ResponseEntity.ok(result);
    }

    /**
     * 역할: 특정 사용자의 최신 성격 검사 결과를 조회합니다.
     */
    @GetMapping("/results/{userId}")
    // ✨ 2. Service에서 반환하는 Optional을 올바르게 처리하도록 수정
    public ResponseEntity<PersonalityTestResult> getPersonalityTestResult(@PathVariable String userId) {
        return personalityService.getPersonalityTestResult(userId)
                // Optional에 결과가 있으면(map), 200 OK 응답으로 감싸고
                .map(ResponseEntity::ok)
                // Optional이 비어있으면(orElseGet), 404 Not Found 응답을 생성합니다.
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // ✨ 특정 사용자의 모든 검사 기록을 조회하는 API (새로 추가)
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PersonalityTestResult>> getTestHistory(@PathVariable String userId) {
        List<PersonalityTestResult> history = personalityService.getTestHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }
}