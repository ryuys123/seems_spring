package com.test.seems.test.controller;

import com.test.seems.counseling.model.dto.CounselingDto;
import com.test.seems.counseling.model.service.CounselingService;
import com.test.seems.test.model.dto.*;
import com.test.seems.test.model.service.PersonalityService;
import com.test.seems.test.model.service.PsychologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/psychological-test")
@RequiredArgsConstructor
public class PsychologicalController {

    private final PsychologyService psychologyService;
    private final PersonalityService personalityService;
    private final CounselingService counselingService;

    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getPsychologicalQuestions(
            @RequestParam(name = "count", required = false, defaultValue = "3") int count,
            @RequestParam(name = "testType") String testType) {
        List<TestQuestion> questions = psychologyService.getMultipleRandomQuestionsByType(count, testType);
        if (questions != null && !questions.isEmpty()) {
            return ResponseEntity.ok(questions);
        } else {
            log.warn("테스트 타입 '{}'에 해당하는 심리 검사 문항을 찾을 수 없습니다.", testType);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ✨ 특정 카테고리에 해당하는 모든 문항을 조회합니다. (우울증/스트레스 척도 검사용)
     */
    @GetMapping("/questions/by-category")
    public ResponseEntity<List<TestQuestion>> getQuestionsByCategory(@RequestParam String category) {
        log.info("GET /questions/by-category?category={} 호출됨.", category);
        List<TestQuestion> questions = psychologyService.getQuestionsByTestTypeAndCategory(category);
        if (questions != null && !questions.isEmpty()) {
            return ResponseEntity.ok(questions);
        } else {
            log.warn("카테고리 '{}'에 해당하는 문항을 찾을 수 없습니다.", category);
            return ResponseEntity.notFound().build();
        }
    }

    // 수정 후
    @PostMapping("/scale")
    public ResponseEntity<PsychologicalTestResultResponse> submitScaleTest(@RequestBody ScaleTestSubmission submissionDto) {
        try {
            log.info("POST /api/psychological-test/scale 호출됨. Category: {}, User: {}", submissionDto.getTestCategory(), submissionDto.getUserId());
            // 서비스 계층은 이제 엔티티가 아닌 DTO를 반환해야 합니다.
            PsychologicalTestResultResponse result = psychologyService.saveScaleTestResult(submissionDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("척도 검사 제출 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/submit-answer")
    public ResponseEntity<PsychologicalTestResultResponse> submitPsychologicalAnswer(
            @RequestBody PsychologicalAnswerRequest answerRequest
    ) {
        log.info("POST /api/psychological-test/submit-answer (이미지-텍스트 분석) 호출됨. Request: {}", answerRequest);
        PsychologicalTestResultResponse result = psychologyService.submitPsychologicalAnswerSequentially(answerRequest);

        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            log.info("이미지-텍스트 분석 검사 중간 단계 답변 제출 완료.");
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/result/{resultId}")
    public ResponseEntity<PsychologicalTestResultResponse> getPsychologicalTestResult(
            @PathVariable Long resultId,
            @RequestParam(name = "testType") String testType
    ) {
        log.info("GET /result/{}?type={} 호출됨. 결과 조회 요청.", resultId, testType);
        PsychologicalTestResultResponse result = psychologyService.getPsychologicalTestResult(resultId, testType);
        if (result != null) {
            log.info("결과 조회 성공: Result ID: {}", result.getResultId());
            return ResponseEntity.ok(result);
        } else {
            log.warn("결과 ID {} (Type: {})를 찾을 수 없거나 DTO 변환에 실패했습니다.", resultId, testType);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/latest-image-result/{userId}")
    public ResponseEntity<PsychologicalTestResultResponse> getLatestImageResult(@PathVariable String userId) {
        return psychologyService.getLatestPsychologicalImageResult(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("사용자 '{}'의 최신 이미지 심리 검사 결과를 찾을 수 없습니다.", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/latest-scale-result/{userId}/{testCategory}")
    public ResponseEntity<PsychologicalTestResultResponse> getLatestScaleResult(
            @PathVariable String userId,
            @PathVariable String testCategory
    ) {
        return psychologyService.getLatestScaleResult(userId, testCategory)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("사용자 '{}'의 최신 {} 척도 검사 결과를 찾을 수 없습니다.", userId, testCategory);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/latest-result/{userId}")
    public ResponseEntity<PsychologicalTestResultResponse> getLatestResult(@PathVariable String userId) {
        PsychologicalTestResultResponse result = psychologyService.getLatestPsychologicalTestResultByUserId(userId);

        if (result == null) {
            log.warn("사용자 '{}'의 최신 심리 검사 결과를 찾을 수 없습니다.", userId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    // 개선 후 API 시그니처 예시
    @GetMapping("/integrated-result/{userId}") // 단수형으로 변경
    public ResponseEntity<IntegratedAnalysisResult> getIntegratedTestResult(@PathVariable String userId) {
        PersonalityTestResult latestPersonalityResult = personalityService.getPersonalityTestResult(userId).orElse(null);
        PsychologicalTestResultResponse latestImageResult = psychologyService.getLatestPsychologicalImageResult(userId).orElse(null);
        PsychologicalTestResultResponse latestDepressionResult = psychologyService.getLatestScaleResult(userId, "DEPRESSION_SCALE").orElse(null);
        PsychologicalTestResultResponse latestStressResult = psychologyService.getLatestScaleResult(userId, "STRESS_SCALE").orElse(null);
        CounselingDto.DetailResponse latestCounselingSummary = counselingService.getLatestCounselingHistoryDetail(userId).orElse(null);

        IntegratedAnalysisResult integratedResults = IntegratedAnalysisResult.builder()
                .latestPersonalityResult(latestPersonalityResult)
                .latestImageResult(latestImageResult)
                .latestDepressionResult(latestDepressionResult)
                .latestStressResult(latestStressResult)
                .latestCounselingSummary(latestCounselingSummary)
                .build();

        return ResponseEntity.ok(integratedResults); // List로 감싸지 않고 바로 반환
    }
}
