// src/main/java/com/test/seems/test/controller/PsychologicalController.java
package com.test.seems.test.controller;

import com.test.seems.test.model.dto.*;
import com.test.seems.test.model.service.PsychologyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController // 이 클래스가 RESTful API의 컨트롤러임을 선언
@RequestMapping("/api/psychological-test") // 이 컨트롤러의 모든 API 경로의 기본 접두사
public class PsychologicalController {

    private final PsychologyService psychologyService;

    @Autowired
    public PsychologicalController(PsychologyService psychologyService) {
        this.psychologyService = psychologyService;
    }

    @PostConstruct
    public void init() {
        log.info("<<<<< PsychologicalController 가 성공적으로 로드되었습니다! >>>>>");
    }

    // ----------------------------------------------------------------------
    // 1. 공통 문항 조회 (TB_COMMON_QUESTIONS 활용)
    // ----------------------------------------------------------------------

    /**
     * 역할: 이미지 기반 심리 검사를 위한 랜덤 문항 하나를 제공합니다.
     * (TEST_TYPE이 'PSYCHOLOGICAL_IMAGE'이고 CATEGORY가 'IMAGE_BASED'인 문항)
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/image-question
     * 반환 타입: ResponseEntity<TestQuestion> (랜덤 이미지 문항 DTO)
     */
    @GetMapping("/image-question")
    public ResponseEntity<TestQuestion> getImageQuestion() {
        TestQuestion imageQuestion = psychologyService.getRandomImageQuestion();
        if (imageQuestion != null) {
            return ResponseEntity.ok(imageQuestion);
        } else {
            log.warn("이미지 기반 심리 검사 문항을 찾을 수 없습니다.");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 역할: 특정 카테고리(예: 'DEPRESSION_SCALE', 'STRESS_SCALE')의 심리 검사 문항들을 조회합니다.
     * (TEST_TYPE이 'PSYCHOLOGICAL_SCALE'이고 특정 CATEGORY인 문항)
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/questions/{category}
     * 경로 변수: category (조회할 문항 카테고리. 예: 'DEPRESSION_SCALE')
     * 반환 타입: ResponseEntity<List<TestQuestion>>
     */
    @GetMapping("/questions/{category}")
    public ResponseEntity<List<TestQuestion>> getPsychologicalQuestionsByCategory(@PathVariable String category) {
        // 서비스에서 CATEGORY와 TEST_TYPE(PSYCHOLOGICAL_SCALE)을 조합하여 문항을 가져옵니다.
        List<TestQuestion> questions = psychologyService.getQuestionsByTestTypeAndCategory(category);
        if (questions != null && !questions.isEmpty()) {
            return ResponseEntity.ok(questions);
        } else {
            log.warn("카테고리 '{}'에 해당하는 심리 검사 문항을 찾을 수 없습니다.", category);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 역할: 여러 개의 심리 검사 문항을 지정된 `testType`에 따라 랜덤으로 제공합니다.
     * (예: 이미지 기반 검사 문항 3개: /questions?count=3&type=PSYCHOLOGICAL_IMAGE)
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/questions?count={count}&type={type}
     * 쿼리 파라미터: count (가져올 문항 개수, 기본값 3), type (검사 유형, 기본값 PSYCHOLOGICAL_IMAGE)
     * 반환 타입: ResponseEntity<List<TestQuestion>>
     */
    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getMultipleTestQuestions(
            @RequestParam(name = "count", defaultValue = "3") int count,
            @RequestParam(name = "testType", required = false, defaultValue = "PSYCHOLOGICAL_IMAGE") String testType
    ) {
        List<TestQuestion> questions = psychologyService.getMultipleRandomQuestionsByType(count, testType); // ⭐ 수정된 서비스 메서드 호출
        if (questions == null || questions.isEmpty()) {
            log.warn("랜덤 문항을 찾을 수 없거나 DTO 변환에 실패했습니다. (count: {}, type: {})", count, testType);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questions);
    }

    // ----------------------------------------------------------------------
    // 2. 심리 검사 답변 제출
    // ----------------------------------------------------------------------

    /**
     * 역할: 이미지-텍스트 심리 검사의 답변을 제출하고, AI 분석 결과를 반환합니다.
     * (TB_PSYCHOLOGICAL_IMAGE_ANSWERS 및 TB_PSYCHOLOGICAL_IMAGE_RESULTS 활용)
     * HTTP 메서드: POST
     * 엔드포인트: /api/psychological-test/submit-answer
     * 요청 본문: PsychologicalAnswerRequest (사용자 답변 텍스트, 문항 ID, 사용자 ID 등 포함)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (통합 심리 분석 결과 DTO)
     */
    @PostMapping("/submit-answer")
    public ResponseEntity<PsychologicalTestResultResponse> submitPsychologicalAnswer(
            @RequestBody PsychologicalAnswerRequest answerRequest
    ) {
        log.info("POST /api/psychological-test/submit-answer (이미지-텍스트 분석) 호출됨. Request: {}", answerRequest);
        PsychologicalTestResultResponse result = psychologyService.submitPsychologicalAnswerSequentially(answerRequest);

        if (result != null) {
            return ResponseEntity.ok(result); // 마지막 단계여서 최종 결과가 반환된 경우
        } else {
            log.info("이미지-텍스트 분석 검사 중간 단계 답변 제출 완료.");
            return ResponseEntity.noContent().build(); // 아직 중간 단계인 경우 (204 No Content)
        }
    }

    /**
     * 역할: 우울증 척도 검사 답변을 제출하고 결과를 계산하여 반환합니다.
     * (TB_PSYCHOLOGICAL_SCALE_ANSWERS 및 TB_PSYCHOLOGICAL_SCALE_RESULTS 활용)
     * HTTP 메서드: POST
     * 엔드포인트: /api/psychological-test/submit-depression-test
     * 요청 본문: List<PsychologicalScaleAnswerRequest> (우울증 검사 답변 목록)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (통합 심리 분석 결과 DTO)
     */
    @PostMapping("/submit-depression-test")
    public ResponseEntity<PsychologicalTestResultResponse> submitDepressionTest(
            @RequestBody List<PsychologicalScaleAnswerRequest> answersRequest
    ) {
        log.info("POST /api/psychological-test/submit-depression-test 호출됨 - {}개 답변", answersRequest.size());
        PsychologicalTestResultResponse result = psychologyService.submitDepressionTest(answersRequest); // ⭐ 서비스 메서드 호출

        if (result != null) {
            log.info("POST /api/psychological-test/submit-depression-test - 결과 반환 (200 OK)");
            return ResponseEntity.ok(result);
        } else {
            log.warn("우울증 검사 제출 처리 중 결과가 null입니다. 서비스 로직을 확인하세요.");
            return ResponseEntity.internalServerError().build(); // 서비스에서 null 반환 시 500 에러
        }
    }

    /**
     * 역할: 스트레스 척도 검사 답변을 제출하고 결과를 계산하여 반환합니다.
     * (TB_PSYCHOLOGICAL_SCALE_ANSWERS 및 TB_PSYCHOLOGICAL_SCALE_RESULTS 활용)
     * HTTP 메서드: POST
     * 엔드포인트: /api/psychological-test/submit-stress-scale-answers
     * 요청 본문: List<PsychologicalScaleAnswerRequest> (스트레스 검사 답변 목록)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (통합 심리 분석 결과 DTO)
     */
    @PostMapping("/submit-stress-scale-answers")
    public ResponseEntity<PsychologicalTestResultResponse> submitStressScaleAnswers(
            @RequestBody List<PsychologicalScaleAnswerRequest> answersRequest
    ) {
        log.info("POST /api/psychological-test/submit-stress-scale-answers 호출됨 - {}개 답변", answersRequest.size());
        PsychologicalTestResultResponse result = psychologyService.processStressTest(answersRequest); // ⭐ 서비스 메서드 호출
        if (result != null) {
            log.info("POST /api/psychological-test/submit-stress-scale-answers - 결과 반환 (200 OK)");
            return ResponseEntity.ok(result);
        } else {
            log.warn("스트레스 검사 제출 처리 중 결과가 null입니다. 서비스 로직을 확인하세요.");
            return ResponseEntity.internalServerError().build(); // 서비스에서 null 반환 시 500 에러
        }
    }

    /**
     * 역할: 통합 척도 검사 답변을 제출합니다. (DEPRESSION_SCALE 또는 STRESS_SCALE 유형 포함)
     * 이 엔드포인트는 submit-depression-test와 submit-stress-scale-answers를
     * 하나로 통합하고 싶을 때 사용될 수 있습니다. 현재는 위의 두 개별 엔드포인트와 중복되므로
     * 사용하지 않을 경우 삭제를 고려하거나, 이 엔드포인트만 남기고 위 두 개를 삭제할 수 있습니다.
     * HTTP 메서드: POST
     * 엔드포인트: /api/psychological-test/submit-scale-answers
     * 요청 본문: TestSubmissionRequest (answers 리스트와 scaleType 등 포함)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse>
     */

    public ResponseEntity<PsychologicalTestResultResponse> submitScaleAnswers(
            @RequestBody TestSubmissionRequest submissionRequest // TestSubmissionRequest를 받음
    ) {
        log.info("POST /api/psychological-test/submit-scale-answers 호출됨. 답변 개수: {}", submissionRequest.getAnswers().size());
        // 서비스에서 submissionRequest.getScaleType() 등을 사용하여 검사 유형을 구분하고 처리해야 합니다.
        // 현재 PsychologyService에는 processDepressionTest와 processStressTest가 별도로 구현되어 있으므로
        // 이 메서드에서는 submissionRequest.getScaleType()에 따라 해당 서비스 메서드를 호출하도록 구현해야 합니다.
        // 예를 들어:
        // String scaleType = submissionRequest.getScaleType(); // DTO에 scaleType 필드가 있다면
        // if ("DEPRESSION_SCALE".equals(scaleType)) {
        //    return ResponseEntity.ok(psychologyService.submitDepressionTest(submissionRequest.getAnswers()));
        // } else if ("STRESS_SCALE".equals(scaleType)) {
        //    return ResponseEntity.ok(psychologyService.processStressTest(submissionRequest.getAnswers()));
        // } else {
        //    return ResponseEntity.badRequest().build();
        // }

        // 현재는 DTO에 scaleType이 없다고 가정하고, 이 엔드포인트는 잠정적으로 불필요하다고 판단하거나
        // 추후 통합을 위해 남겨두는 것으로 처리 (여기서는 임시로 internalServerError 반환)
        log.warn("submit-scale-answers 엔드포인트가 호출되었으나, DTO에 scaleType이 없거나 로직이 구현되지 않았습니다.");
        return ResponseEntity.internalServerError().build();
    }


    // ----------------------------------------------------------------------
    // 3. 심리 검사 결과 조회
    // ----------------------------------------------------------------------

    /**
     * 역할: 특정 심리 검사 결과(리포트)를 조회합니다.
     * (testType에 따라 TB_PSYCHOLOGICAL_IMAGE_RESULTS 또는 TB_PSYCHOLOGICAL_SCALE_RESULTS에서 조회)
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/result/{resultId}?type={testType}
     * 경로 변수: resultId (조회하고자 하는 결과의 고유 ID)
     * 쿼리 파라미터: type (검사 유형, 예: "depression", "stress", "image") - ⭐ 필수!
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (통합 심리 분석 결과 DTO)
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<PsychologicalTestResultResponse> getPsychologicalTestResult(
            @PathVariable Long resultId,
            @RequestParam(name = "testType") String testType // ⭐ testType을 필수로 받도록 변경
    ) {
        log.info("GET /result/{}?type={} 호출됨. 결과 조회 요청.", resultId, testType);
        // 서비스에서 testType에 따라 이미지/척도 검사 결과를 분기하여 조회할 것입니다.
        PsychologicalTestResultResponse result = psychologyService.getPsychologicalTestResult(resultId, testType);
        if (result != null) {
            log.info("결과 조회 성공: Result ID: {}", result.getResultId());
            return ResponseEntity.ok(result);
        } else {
            log.warn("결과 ID {} (Type: {})를 찾을 수 없거나 DTO 변환에 실패했습니다.", resultId, testType);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 역할: 특정 사용자 ID의 가장 최근 심리 분석 결과를 조회합니다.
     * (이미지 검사, 우울증 검사, 스트레스 검사 결과를 모두 고려하여 가장 최근 결과를 반환)
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/latest-result/{userId}
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (통합 심리 분석 결과 DTO)
     */
    @GetMapping("/latest-result/{userId}")
    public ResponseEntity<PsychologicalTestResultResponse> getLatestResult(@PathVariable String userId) {
        // Service를 통해 해당 사용자의 최신 결과를 조회합니다.
        // 서비스 로직에서 TB_PSYCHOLOGICAL_IMAGE_RESULTS와 TB_PSYCHOLOGICAL_SCALE_RESULTS를 모두 고려합니다.
        PsychologicalTestResultResponse result = psychologyService.getLatestPsychologicalTestResultByUserId(userId);

        if (result == null) {
            log.warn("사용자 '{}'의 최신 심리 검사 결과를 찾을 수 없습니다.", userId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
    @PostMapping("/submit-stress-test")
    public ResponseEntity<PsychologicalTestResultResponse> submitStressTest(@RequestBody List<PsychologicalScaleAnswerRequest> answersRequest) {
        PsychologicalTestResultResponse result = psychologyService.submitStressTest(answersRequest);
        return ResponseEntity.ok(result);
    }
}