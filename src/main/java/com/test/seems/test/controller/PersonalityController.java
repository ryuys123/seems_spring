package com.test.seems.test.controller;

import com.test.seems.test.model.dto.PersonalitySubmissionDto;
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.service.PersonalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personality-test")
public class PersonalityController {

    private final PersonalityService personalityService;

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

    @PostMapping("/submit")
    public ResponseEntity<PersonalityTestResult> submitPersonalityTest(
            @RequestBody PersonalitySubmissionDto submissionDto
    ) {
        try {
            log.info("POST /api/personality-test/submit 호출됨 - User: {}, 답변 {}개", submissionDto.getUserId(), submissionDto.getAnswers().size());
            PersonalityTestResult result = personalityService.submitPersonalityTest(submissionDto);
            log.info("성격 검사 결과 제출 완료. MBTI 유형: {}", result.getMbtiType());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("MBTI 검사 제출 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/results/{userId}")
    public ResponseEntity<PersonalityTestResult> getPersonalityTestResult(@PathVariable String userId) {
        return personalityService.getPersonalityTestResult(userId)
                .map(result -> {
                    log.info("사용자 '{}'의 최신 성격 검사 결과 조회 성공. MBTI 유형: {}", userId, result.getMbtiType());
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    log.warn("사용자 '{}'의 최신 성격 검사 결과를 찾을 수 없습니다.", userId);
                    return ResponseEntity.notFound().build();
                });
    }

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
