// src/main/java/com/test/seems/test/controller/PsychologicalTestController.java
package com.test.seems.test.controller;

import com.test.seems.test.model.dto.TestQuestion; // 문항 DTO (TestQuestion) 임포트
import com.test.seems.test.model.dto.PsychologicalAnswerRequest; // 사용자 답변 요청 DTO
import com.test.seems.test.model.dto.PsychologicalTestResultResponse; // 심리 검사 결과 응답 DTO
import com.test.seems.test.model.service.PsychologyService; // PsychologyService 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 이 클래스가 RESTful API의 컨트롤러임을 선언
@RequestMapping("/api/psychological-test") // 이 컨트롤러의 모든 API 경로의 기본 접두사
public class PsychologicalController {

    private final PsychologyService psychologyService;

    @Autowired
    public PsychologicalController(PsychologyService psychologyService) {
        this.psychologyService = psychologyService;
    }

    /**
     * 역할: 이미지 기반 심리 검사를 위한 랜덤 문항(이미지 URL과 질문 텍스트) 하나를 제공합니다.
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
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 역할: 사용자가 작성한 '느낀 점' 텍스트를 제출하면, 이를 저장하고 AI 분석 후 결과를 반환합니다.
     * HTTP 메서드: POST
     * 엔드포인트: /api/psychological-test/submit-answer
     * 요청 본문: PsychologicalAnswerRequest (사용자의 답변 텍스트, 문항 ID, 사용자 ID 등)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (심리 분석 결과 DTO)
     */
    @PostMapping("/submit-answer") // submit-answer로 엔드포인트 수정
    public ResponseEntity<PsychologicalTestResultResponse> submitPsychologicalAnswer(
            @RequestBody PsychologicalAnswerRequest answerRequest
    ) {
        PsychologicalTestResultResponse result = psychologyService.submitAnswerAndAnalyze(answerRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 역할: 특정 심리 검사 결과(리포트)를 조회합니다.
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/result/{resultId}
     * 경로 변수: resultId (조회하고자 하는 결과의 고유 ID)
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse> (심리 분석 결과 DTO)
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<PsychologicalTestResultResponse> getPsychologicalTestResult(@PathVariable Long resultId) {
        PsychologicalTestResultResponse result = psychologyService.getPsychologicalTestResult(resultId);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ⭐️ 추가된 엔드포인트: 사용자의 최신 분석 결과 조회
    /**
     * 역할: 특정 사용자 ID의 가장 최근 심리 분석 결과를 조회합니다.
     * HTTP 메서드: GET
     * 엔드포인트: /api/psychological-test/latest-result/{userId}
     * 반환 타입: ResponseEntity<PsychologicalTestResultResponse>
     */
    @GetMapping("/latest-result/{userId}")
    public ResponseEntity<PsychologicalTestResultResponse> getLatestResult(@PathVariable String userId) {

        // Service를 통해 해당 사용자의 최신 결과를 조회합니다.
        PsychologicalTestResultResponse result = psychologyService.getLatestPsychologicalTestResultByUserId(userId);

        if (result == null) {
            // 결과가 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}