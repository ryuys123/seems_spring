package com.test.seems.test.controller;

import com.test.seems.test.model.dto.PersonalityAnswerRequest; // ⭐ 이름 변경된 DTO 임포트
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.service.PersonalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위해 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j // 로그 사용을 위해 추가
@RestController
@RequiredArgsConstructor // final 필드에 대한 생성자를 만들어주므로 @Autowired 생략 가능
@RequestMapping("/api/personality-test")
public class PersonalityController {

    private final PersonalityService personalityService;

    /**
     * 역할: 성격 검사 문항 목록을 프론트엔드에 제공합니다.
     * (TB_COMMON_QUESTIONS 테이블에서 TEST_TYPE이 'PERSONALITY'인 문항 조회)
     */
    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getPersonalityQuestions() {
        List<TestQuestion> questions = personalityService.getPersonalityQuestions();
        if (questions != null && !questions.isEmpty()) {
            return ResponseEntity.ok(questions);
        } else {
            log.warn("성격 검사 문항을 찾을 수 없습니다.");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 역할: 사용자가 성격 검사 답변을 제출하면, 이를 저장하고 MBTI 결과를 계산하여 반환합니다.
     * (TB_PERSONALITY_ANSWERS 및 TB_PERSONALITY_RESULTS 활용)
     * HTTP 메서드: POST
     * 엔드포인트: /api/personality-test/submit-answers
     * 요청 본문: List<PersonalityAnswerRequest> (성격 검사 답변 목록)
     * 반환 타입: ResponseEntity<PersonalityTestResult>
     */
    @PostMapping("/submit-answers")
    public ResponseEntity<PersonalityTestResult> submitPersonalityAnswers(
            @RequestBody List<PersonalityAnswerRequest> answers // ⭐ 이름 변경된 DTO 사용
    ) {
        log.info("POST /api/personality-test/submit-answers 호출됨 - {}개 답변", answers.size());
        PersonalityTestResult result = personalityService.submitAnswersAndCalculateResult(answers);
        log.info("성격 검사 결과 제출 완료. MBTI 유형: {}", result.getMbtiTitle());
        return ResponseEntity.ok(result);
    }

    /**
     * 역할: 특정 사용자의 최신 성격 검사 결과를 조회합니다.
     * (TB_PERSONALITY_RESULTS 테이블에서 조회)
     * HTTP 메서드: GET
     * 엔드포인트: /api/personality-test/results/{userId}
     * 경로 변수: userId (조회할 사용자의 고유 ID)
     * 반환 타입: ResponseEntity<PersonalityTestResult>
     */
    @GetMapping("/results/{userId}")
    public ResponseEntity<PersonalityTestResult> getPersonalityTestResult(@PathVariable String userId) {
        return personalityService.getPersonalityTestResult(userId)
                .map(result -> {
                    log.info("사용자 '{}'의 최신 성격 검사 결과 조회 성공. MBTI 유형: {}", userId, result.getMbtiTitle());
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    log.warn("사용자 '{}'의 최신 성격 검사 결과를 찾을 수 없습니다.", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 역할: 특정 사용자 ID의 모든 성격 검사 기록을 조회합니다.
     * (TB_PERSONALITY_RESULTS 테이블에서 조회)
     * HTTP 메서드: GET
     * 엔드포인트: /api/personality-test/history/{userId}
     * 경로 변수: userId (조회할 사용자의 고유 ID)
     * 반환 타입: ResponseEntity<List<PersonalityTestResult>>
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PersonalityTestResult>> getTestHistory(@PathVariable String userId) {
        List<PersonalityTestResult> history = personalityService.getTestHistoryByUserId(userId);
        if (history != null && !history.isEmpty()) {
            log.info("사용자 '{}'의 성격 검사 기록 {}개 조회 성공.", userId, history.size());
            return ResponseEntity.ok(history);
        } else {
            log.warn("사용자 '{}'의 성격 검사 기록을 찾을 수 없습니다.", userId);
            return ResponseEntity.notFound().build();
        }
    }
}