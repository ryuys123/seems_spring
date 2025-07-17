// src/main/java/com/test/seems/test/model/service/PsychologyService.java
package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import com.test.seems.test.jpa.entity.PsychologyEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.repository.*;
import com.test.seems.test.model.dto.PsychologicalAnswerRequest;
import com.test.seems.test.model.dto.PsychologicalScaleAnswerRequest;
import com.test.seems.test.model.dto.PsychologicalTestResultResponse;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.test.model.entity.ScaleAnalysisResultEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PsychologyService {

    private final CommonQuestionRepository commonQuestionRepository;
    private final PsychologicalImageAnswerRepository psychologicalImageAnswerRepository; // 이미지 답변 리포지토리
    private final PsychologicalImageResultRepository psychologicalImageResultRepository; // 이미지 결과 리포지토리
    private final ScaleAnalysisResultRepository scaleAnalysisResultRepository; // ⭐ 척도 결과 리포지토리
    private final PsychologicalScaleAnswerRepository psychologicalScaleAnswerRepository; // ⭐ 척도 답변 리포지토리
    private final WebClient pythonAiWebClient;

    @Autowired // ⭐ 모든 변경된 리포지토리를 생성자를 통해 주입받습니다. ⭐
    public PsychologyService(CommonQuestionRepository commonQuestionRepository,
                             PsychologicalImageAnswerRepository psychologicalImageAnswerRepository,
                             PsychologicalImageResultRepository psychologicalImageResultRepository,
                             ScaleAnalysisResultRepository scaleAnalysisResultRepository,
                             PsychologicalScaleAnswerRepository psychologicalScaleAnswerRepository,
                             WebClient pythonAiWebClient) {
        this.commonQuestionRepository = commonQuestionRepository;
        this.psychologicalImageAnswerRepository = psychologicalImageAnswerRepository;
        this.psychologicalImageResultRepository = psychologicalImageResultRepository;
        this.scaleAnalysisResultRepository = scaleAnalysisResultRepository;
        this.psychologicalScaleAnswerRepository = psychologicalScaleAnswerRepository;
        this.pythonAiWebClient = pythonAiWebClient;
    }

    // ----------------------------------------------------------------------
    // 공통 문항 조회 메서드 (TestQuestionEntity, TB_COMMON_QUESTIONS 활용)
    // ----------------------------------------------------------------------

    /**
     * 역할: DB에서 특정 `testType` (예: 'PSYCHOLOGICAL_IMAGE', 'PSYCHOLOGICAL_SCALE')의 문항을
     * 지정된 개수(count)만큼 랜덤으로 선택하여 리스트로 반환합니다.
     * @param count 가져올 문항의 개수
     * @param testType 조회할 문항의 대분류 타입 (예: "PSYCHOLOGICAL_IMAGE", "PSYCHOLOGICAL_SCALE")
     * @return List<TestQuestion> (랜덤 문항 DTO 리스트)
     */
    @Transactional(readOnly = true)
    public List<TestQuestion> getMultipleRandomQuestionsByType(int count, String testType) { // ⭐ 메서드명 변경 및 testType 인자 추가
        List<TestQuestionEntity> questions = commonQuestionRepository.findByTestType(testType); // ⭐ testType으로만 조회

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(questions, new Random()); // 리스트를 무작위로 섞습니다.

        int limit = Math.min(count, questions.size());
        return questions.stream()
                .limit(limit)
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 역할: 특정 `category` (예: 'DEPRESSION_SCALE', 'STRESS_SCALE', 'MBTI_E_I')의 문항들을 조회합니다.
     * @param category 조회할 문항 세부 카테고리
     * @return 해당 카테고리의 TestQuestion DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TestQuestion> getQuestionsByTestTypeAndCategory(String category) {
        // ⭐ CATEGORY로 질문을 가져올 때 TEST_TYPE도 함께 필터링하는 것이 좋습니다.
        // 예를 들어, 'DEPRESSION_SCALE'은 'PSYCHOLOGICAL_SCALE' 타입에 속합니다.
        // 여기서는 편의상 `findByCategory`만 사용하지만, 실제로는 `findByTestTypeAndCategory`를 활용해야 합니다.
        // CommonQuestionRepository에 findByCategory(String category) 또는 findByTestTypeAndCategory(String testType, String category) 메서드 필요
        List<TestQuestionEntity> questions = commonQuestionRepository.findByCategory(category); // ⭐ findByCategory 사용 가정
        return questions.stream()
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * (기존) 역할: DB에서 이미지 기반 질문 하나를 랜덤으로 선택하여 반환합니다.
     * (TestQuestionRepository가 CommonQuestionRepository로 변경됨에 따라 내부 로직 조정)
     * @return TestQuestion (랜덤 이미지 문항 DTO)
     */
    @Transactional(readOnly = true)
    public TestQuestion getRandomImageQuestion() {
        // TEST_TYPE이 'PSYCHOLOGICAL_IMAGE'이고 CATEGORY가 'IMAGE_BASED'인 질문들을 조회합니다.
        List<TestQuestionEntity> imageBasedQuestions = commonQuestionRepository.findByTestTypeAndCategory("PSYCHOLOGICAL_IMAGE", "IMAGE_BASED");

        if (imageBasedQuestions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        TestQuestionEntity randomQuestionEntity = imageBasedQuestions.get(random.nextInt(imageBasedQuestions.size()));

        return randomQuestionEntity.toDto();
    }


    // ----------------------------------------------------------------------
    // 심리 검사 답변 제출 및 분석 메서드
    // ----------------------------------------------------------------------

    /**
     * 역할: 사용자의 이미지-텍스트 심리 검사 답변(느낀 점 텍스트)을 저장하고,
     * 마지막 단계 답변일 경우 이전에 제출된 모든 답변을 종합 분석하여 결과를 반환합니다.
     * @param answerRequest 단계 정보가 포함된 사용자 답변 DTO
     * @return PsychologicalTestResultResponse (마지막 단계일 때만 결과 반환, 중간 단계일 때는 null 반환)
     */
    @Transactional
    public PsychologicalTestResultResponse submitPsychologicalAnswerSequentially(PsychologicalAnswerRequest answerRequest) {

        // ✅✅✅✅✅ 이 안에 코드를 넣어주세요 ✅✅✅✅✅
        // 1. 현재 답변을 DB에 저장하는 로직입니다.
        // 기존의 psychologicalImageAnswerRepository.save(answerRequest.toEntity()); 한 줄을 지우고,
        // 아래 코드를 여기에 붙여넣어 주세요.

        PsychologyEntity answerEntity = PsychologyEntity.builder()
                .userId(answerRequest.getUserId())
                .questionId(answerRequest.getQuestionId())
                .userResponseText(answerRequest.getUserResponseText())
                .testType(answerRequest.getTestType()) // ✨ answerRequest 사용
                .answerDatetime(LocalDateTime.now())
                .build();

        psychologicalImageAnswerRepository.save(answerEntity);
        // 1. 현재 답변을 DB (TB_PSYCHOLOGICAL_IMAGE_ANSWERS)에 저장합니다.

        int currentStep = answerRequest.getCurrentStep();
        int totalSteps = answerRequest.getTotalSteps();

        // 2. 마지막 단계가 아니면, 여기서 작업을 종료하고 null을 반환합니다.
        if (currentStep < totalSteps) {
            return null;
        }

        // 3. 마지막 단계인 경우, 종합 분석을 시작합니다.
        String userId = answerRequest.getUserId();

        // 3-1. DB에서 이 사용자의 최근 답변들을 totalSteps 개수만큼 가져옵니다.
        Pageable pageable = PageRequest.of(0, totalSteps, Sort.by(Sort.Direction.DESC, "answerDatetime"));
        List<PsychologyEntity> recentAnswers = psychologicalImageAnswerRepository.findByUserIdOrderByAnswerDatetimeDesc(userId, pageable);

        // 3-2. 가져온 답변은 최신순이므로, 검사 순서(시간순)에 맞게 뒤집어줍니다. (가장 오래된 답변이 맨 앞으로)
        Collections.reverse(recentAnswers);

        // 3-3. AI 서버로 보낼 요청 데이터를 구성합니다. (모든 답변 텍스트를 결합하여 리스트로 전송)
        List<Map<String, Object>> userResponsesForAi = recentAnswers.stream()
                .map(answer -> Map.<String, Object>of(
                        "questionId", answer.getQuestionId(),
                        "userResponseText", answer.getUserResponseText()
                ))
                .collect(Collectors.toList());

        Map<String, Object> aiRequestBody = Map.of(
                "userId", userId,
                "responses", userResponsesForAi // Python AI 서버가 'responses' 리스트를 기대함
        );

        log.info("AI 서버로 전송할 aiRequestBody: {}", aiRequestBody);

        // 3-4. AI 서버에 분석 요청을 보내고 결과를 받습니다.
        PsychologicalTestResultResponse aiResponseDto;
        try {
            aiResponseDto = pythonAiWebClient.post()
                    .uri("/analyze-psychology")
                    .bodyValue(aiRequestBody)
                    .retrieve()
                    .bodyToMono(PsychologicalTestResultResponse.class)
                    .block();

            if (aiResponseDto == null) {
                throw new RuntimeException("Python AI 서버로부터 유효한 응답을 받지 못했습니다. (응답 NULL)");
            }
        } catch (Exception e) {
            log.error("Python AI 서버 통신 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 프론트엔드에 전달할 더미 결과 (오류 메시지 포함)
            aiResponseDto = PsychologicalTestResultResponse.builder()
                    .userId(userId)
                    .questionId(recentAnswers.get(0).getQuestionId())
                    .rawResponseText(recentAnswers.stream().map(PsychologyEntity::getUserResponseText).collect(Collectors.joining("\n---\n")))
                    .aiSentiment("분석불가")
                    .aiSentimentScore(0.0)
                    .aiCreativityScore(0.0)
                    .aiPerspectiveKeywords("오류")
                    .aiInsightSummary("AI 분석 서버와 통신할 수 없습니다. (오류: " + e.getMessage() + "). AI 서버가 정상적으로 작동하는지 확인해주세요.")
                    .suggestions("AI 서버와 연결에 문제가 발생했습니다. 다시 시도하거나 관리자에게 문의하세요.")
                    .testDateTime(LocalDateTime.now())
                    .build();
        }

        // 3-5. 최종 분석 결과를 DB (TB_PSYCHOLOGICAL_IMAGE_RESULTS)에 저장합니다.
        PsychologicalTestResultEntity resultEntity = PsychologicalTestResultEntity.builder()
                .userId(aiResponseDto.getUserId())
                .questionId(aiResponseDto.getQuestionId())
                .rawResponseText(aiResponseDto.getRawResponseText())
                .testType("IMAGE_TEST") // 이미지 검사임을 명시
                .aiSentiment(aiResponseDto.getAiSentiment())
                .aiSentimentScore(aiResponseDto.getAiSentimentScore())
                .aiCreativityScore(aiResponseDto.getAiCreativityScore())
                .aiPerspectiveKeywords(aiResponseDto.getAiPerspectiveKeywords())
                .aiInsightSummary(aiResponseDto.getAiInsightSummary())
                .suggestions(aiResponseDto.getSuggestions())
                .createdAt(LocalDateTime.now())
                .build();

        // AI 응답 DTO에서 totalScore, diagnosisCategory, interpretationText는 기본적으로 null일 것
        // 이를 DTO 변환 시 잘 처리하도록 PsychologicalTestResultResponse에 필드가 있음을 가정

        psychologicalImageResultRepository.save(resultEntity);

        // 3-6. 저장된 엔티티를 클라이언트로 보낼 DTO (통합 PsychologicalTestResultResponse)로 변환하여 반환합니다.
        return resultEntity.toDto();
    }

    /**
     * 역할: 사용자가 제출한 우울증 검사 답변을 저장하고, 점수를 합산하여 결과를 계산 후 반환합니다.
     * (이 메서드는 AI 분석을 직접 호출하지 않고, 점수 기반 로직을 가집니다.)
     * @param answersRequest 우울증 검사 답변 리스트 (List<PsychologicalScaleAnswerRequest>)
     * @return PsychologicalTestResultResponse (통합 심리 분석 결과 DTO)
     */
    @Transactional
    public PsychologicalTestResultResponse submitDepressionTest(List<PsychologicalScaleAnswerRequest> answersRequest) {
        String userId = answersRequest.get(0).getUserId();

        // 1. 각 답변을 DB (TB_PSYCHOLOGICAL_SCALE_ANSWERS)에 저장합니다.
        for (PsychologicalScaleAnswerRequest dto : answersRequest) {
            // PsychologicalScaleAnswerRequest의 toEntity()가 ScaleTestAnswerEntity를 반환하도록 수정하거나
            // 여기서 직접 ScaleTestAnswerEntity를 빌드하여 저장
            psychologicalScaleAnswerRepository.save(
                    com.test.seems.test.model.entity.ScaleTestAnswerEntity.builder()
                            .userId(dto.getUserId())
                            .questionId(dto.getQuestionId())
                            .answerValue(dto.getAnswerValue())
                            .testCategory("DEPRESSION_SCALE") // 우울증 검사임을 명시
                            .testType("PSYCHOLOGICAL_SCALE")   // 👈 이 라인이 반드시 있어야 합니다.
                            .answerDatetime(LocalDateTime.now())
                            .build()
            );
        }

        // 2. 총점 계산
        int totalScore = answersRequest.stream()
                .mapToInt(PsychologicalScaleAnswerRequest::getAnswerValue)
                .sum();

        // 3. 진단 카테고리, 해석, 위험 수준, 제안 결정 (총점에 따른 로직 구현)
        String diagnosisCategory;
        String interpretationText;
        String riskLevel;
        String suggestions;

        if (totalScore <= 4) { // PHQ-9 기준 (예시)
            diagnosisCategory = "정상";
            interpretationText = "현재 우울감 수준이 정상 범위입니다. 건강한 상태를 유지하고 계십니다.";
            riskLevel = "NORMAL";
            suggestions = "현재의 긍정적인 상태를 잘 유지하세요.";
        } else if (totalScore <= 9) {
            diagnosisCategory = "경미한 우울";
            interpretationText = "경미한 우울감이 감지됩니다. 가벼운 스트레스나 기분 변화일 수 있습니다.";
            riskLevel = "LOW_RISK"; // 새로운 위험 수준
            suggestions = "휴식, 취미 활동, 가벼운 운동 등을 통해 기분 전환을 시도해보세요.";
        } else if (totalScore <= 14) {
            diagnosisCategory = "중간 정도 우울";
            interpretationText = "중간 정도의 우울감이 나타납니다. 일상생활에 영향을 줄 수 있습니다.";
            riskLevel = "MEDIUM_RISK"; // 새로운 위험 수준
            suggestions = "심리 상담 전문가와 이야기를 나눠보거나, 규칙적인 생활과 충분한 수면을 취하는 것이 중요합니다.";
        } else if (totalScore <= 19) {
            diagnosisCategory = "중증도 우울";
            interpretationText = "중증도의 우울감이 감지됩니다. 전문가의 도움이 필요할 수 있습니다.";
            riskLevel = "HIGH_RISK";
            suggestions = "반드시 정신건강의학과 전문의 또는 심리 상담사와 상담을 진행해 보세요. 주변의 지지체계를 활용하는 것도 좋습니다.";
        } else {
            diagnosisCategory = "심한 우울";
            interpretationText = "심한 우울감이 나타나고 있습니다. 즉각적인 전문가의 도움이 필요합니다.";
            riskLevel = "CRITICAL_RISK"; // 새로운 위험 수준
            suggestions = "지체하지 말고 즉시 정신건강의학과 전문의와 상담을 시작하고 필요한 치료를 받으세요. 가족이나 친구에게 도움을 요청하세요.";
        }

        // 4. 결과를 DB (TB_PSYCHOLOGICAL_SCALE_RESULTS)에 저장
        com.test.seems.test.model.entity.ScaleAnalysisResultEntity scaleResultEntity = com.test.seems.test.model.entity.ScaleAnalysisResultEntity.builder()
                .userId(userId)
                .testType("PSYCHOLOGICAL_SCALE") // ✨ 이 라인을 추가하세요.
                .testCategory("DEPRESSION_SCALE") // 검사 유형 명시
                .totalScore((double) totalScore) // Double 타입에 맞춤
                .interpretation(interpretationText)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .createdAt(LocalDateTime.now())
                .build();

        scaleAnalysisResultRepository.save(scaleResultEntity); // 척도 검사 결과 저장

        // 5. 저장된 엔티티를 통합 DTO로 변환하여 반환
        return scaleResultEntity.toDto();
    }

    /**
     * 역할: 사용자가 제출한 스트레스 검사 답변을 저장하고, 점수를 합산하여 결과를 계산 후 반환합니다.
     * @param answersRequest 스트레스 검사 답변 리스트 (List<PsychologicalScaleAnswerRequest>)
     * @return PsychologicalTestResultResponse (통합 심리 분석 결과 DTO)
     */
    @Transactional
    public PsychologicalTestResultResponse processStressTest(List<PsychologicalScaleAnswerRequest> answersRequest) {
        String userId = answersRequest.get(0).getUserId();

        // 1. 각 답변을 DB (TB_PSYCHOLOGICAL_SCALE_ANSWERS)에 저장합니다.
        for (PsychologicalScaleAnswerRequest dto : answersRequest) {
            psychologicalScaleAnswerRepository.save(
                    com.test.seems.test.model.entity.ScaleTestAnswerEntity.builder()
                            .userId(dto.getUserId())
                            .questionId(dto.getQuestionId())
                            .answerValue(dto.getAnswerValue())
                            .testCategory("STRESS_SCALE") // 스트레스 검사임을 명시
                            .answerDatetime(LocalDateTime.now())
                            .build()
            );
        }

        // 2. 총점 계산
        int totalScore = answersRequest.stream()
                .mapToInt(PsychologicalScaleAnswerRequest::getAnswerValue)
                .sum();

        // 3. 진단 카테고리, 해석, 위험 수준, 제안 결정 (총점에 따른 로직 구현)
        String diagnosisCategory;
        String interpretationText;
        String riskLevel;
        String suggestions;

        // 스트레스 PSS 척도 기준 (예시)
        if (totalScore <= 13) {
            diagnosisCategory = "정상";
            interpretationText = "스트레스 수준이 낮은 편입니다. 스트레스 관리를 잘 하고 계십니다.";
            riskLevel = "NORMAL";
            suggestions = "현재의 긍정적인 상태를 잘 유지하고, 스트레스 해소 활동을 꾸준히 해보세요.";
        } else if (totalScore <= 26) {
            diagnosisCategory = "주의";
            interpretationText = "중간 정도의 스트레스 수준을 보입니다. 일상에서 스트레스 요인을 관리할 필요가 있습니다.";
            riskLevel = "CAUTION";
            suggestions = "스트레스 원인을 파악하고, 명상, 취미, 휴식 등 자신에게 맞는 해소법을 찾아 실천해보세요.";
        } else {
            diagnosisCategory = "높음";
            interpretationText = "높은 수준의 스트레스를 경험하고 있습니다. 적극적인 대처가 필요합니다.";
            riskLevel = "HIGH_RISK";
            suggestions = "스트레스로 인해 어려움을 겪고 있다면, 전문가(심리 상담사, 정신과 의사)와 상담을 고려해보세요. 규칙적인 생활과 충분한 수면도 중요합니다.";
        }

        // 4. 결과를 DB (TB_PSYCHOLOGICAL_SCALE_RESULTS)에 저장
        com.test.seems.test.model.entity.ScaleAnalysisResultEntity scaleResultEntity = com.test.seems.test.model.entity.ScaleAnalysisResultEntity.builder()
                .userId(userId)
                .testType("PSYCHOLOGICAL_SCALE") // ✨ 이 라인을 추가하세요.
                .testCategory("STRESS_SCALE") // 검사 유형 명시
                .totalScore((double) totalScore)
                .interpretation(interpretationText)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .createdAt(LocalDateTime.now())
                .build();

        scaleAnalysisResultRepository.save(scaleResultEntity);

        // 5. 저장된 엔티티를 통합 DTO로 변환하여 반환
        return scaleResultEntity.toDto();
    }


    /**
     * ⭐ 수정된 메서드
     * 역할: 특정 심리 검사 결과(리포트)를 DB에서 조회하여 DTO 형태로 반환합니다.
     * `testType` 인자를 통해 이미지 검사 결과와 척도 검사 결과를 구분하여 조회합니다.
     * @param resultId 조회할 결과의 고유 ID
     * @param testType 조회할 검사의 유형 ("image", "depression", "stress")
     * @return PsychologicalTestResultResponse (통합 심리 분석 결과 DTO)
     * @throws RuntimeException 결과를 찾을 수 없거나 알 수 없는 testType인 경우
     */
    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getPsychologicalTestResult(Long resultId, String testType) {
        if (testType == null || testType.trim().isEmpty()) {
            log.warn("getPsychologicalTestResult 호출 시 testType이 누락되었습니다. resultId: {}", resultId);
            throw new IllegalArgumentException("검사 유형(testType)은 필수입니다.");
        }

        // `switch`문이 "IMAGE_TEST"를 포함한 모든 경우를 정확히 처리하도록 합니다.
        switch (testType) {
            case "IMAGE_TEST": // ✨ 이 case가 정확히 일치해야 합니다.
                return psychologicalImageResultRepository.findById(resultId)
                        .map(PsychologicalTestResultEntity::toDto)
                        .orElseThrow(() -> new RuntimeException("이미지 심리 검사 결과를 찾을 수 없습니다. (ID: " + resultId + ")"));

            case "DEPRESSION_SCALE":
            case "PSYCHOLOGICAL_SCALE": // 👈 이 case를 추가합니다.
            case "STRESS_SCALE":
                return scaleAnalysisResultRepository.findById(resultId)
                        .map(com.test.seems.test.model.entity.ScaleAnalysisResultEntity::toDto)
                        .orElseThrow(() -> new RuntimeException(testType + " 척도 검사 결과를 찾을 수 없습니다. (ID: " + resultId + ")"));

            default:
                log.warn("알 수 없는 testType: {} (resultId: {})", testType, resultId);
                throw new IllegalArgumentException("알 수 없는 검사 유형입니다: " + testType);
        }
    }


    /**
     * 역할: 특정 사용자 ID의 가장 최근 심리 분석 결과를 DB에서 조회하여 DTO 형태로 반환합니다.
     * (이미지 검사, 우울증 검사, 스트레스 검사 결과를 모두 고려하여 가장 최근 결과를 반환)
     * @param userId 조회할 사용자의 고유 ID
     * @return PsychologicalTestResultResponse (가장 최근 결과 DTO)
     */
    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getLatestPsychologicalTestResultByUserId(String userId) {
        // 이미지 검사 최신 결과
        Optional<PsychologicalTestResultEntity> latestImageResult = psychologicalImageResultRepository.findTop1ByUserIdOrderByCreatedAtDesc(userId);

        // 척도 검사 최신 결과
        Optional<com.test.seems.test.model.entity.ScaleAnalysisResultEntity> latestScaleResult = scaleAnalysisResultRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

        // 두 결과를 비교하여 최종적으로 가장 최근 결과를 선택
        PsychologicalTestResultResponse result = null;

        if (latestImageResult.isPresent() && latestScaleResult.isPresent()) {
            // 둘 다 존재하면 더 최근의 결과를 선택
            if (latestImageResult.get().getCreatedAt().isAfter(latestScaleResult.get().getCreatedAt())) {
                result = latestImageResult.get().toDto();
            } else {
                result = latestScaleResult.get().toDto();
            }
        } else if (latestImageResult.isPresent()) {
            result = latestImageResult.get().toDto();
        } else if (latestScaleResult.isPresent()) {
            result = latestScaleResult.get().toDto();
        }
        return result;
    }

    @Transactional
    public PsychologicalTestResultResponse submitStressTest(List<PsychologicalScaleAnswerRequest> answersRequest) {
        String userId = answersRequest.get(0).getUserId();

        // 1. 각 답변을 DB에 저장합니다.
        for (PsychologicalScaleAnswerRequest dto : answersRequest) {
            psychologicalScaleAnswerRepository.save(
                    com.test.seems.test.model.entity.ScaleTestAnswerEntity.builder()
                            .userId(dto.getUserId())
                            .questionId(dto.getQuestionId())
                            .answerValue(dto.getAnswerValue())
                            .testType(dto.getTestType()) // testType을 DTO에서 가져옵니다.
                            .testCategory("STRESS_SCALE") // 스트레스 검사임을 명시
                            .answerDatetime(LocalDateTime.now())
                            .build()
            );
        }

        // 2. 총점을 계산합니다.
        int totalScore = answersRequest.stream()
                .mapToInt(PsychologicalScaleAnswerRequest::getAnswerValue)
                .sum();

        // 3. 점수에 따른 진단 로직을 구현합니다.
        String interpretationText;
        String riskLevel;
        String suggestions;

        // PSS (Perceived Stress Scale) 척도 기준 예시
        if (totalScore <= 13) {
            interpretationText = "스트레스 수준이 낮은 편입니다. 스트레스 관리를 잘 하고 계십니다.";
            riskLevel = "NORMAL";
            suggestions = "현재의 긍정적인 상태를 잘 유지하고, 스트레스 해소 활동을 꾸준히 해보세요.";
        } else if (totalScore <= 26) {
            interpretationText = "중간 정도의 스트레스 수준을 보입니다. 일상에서 스트레스 요인을 관리할 필요가 있습니다.";
            riskLevel = "CAUTION";
            suggestions = "스트레스 원인을 파악하고, 명상, 취미, 휴식 등 자신에게 맞는 해소법을 찾아 실천해보세요.";
        } else {
            interpretationText = "높은 수준의 스트레스를 경험하고 있습니다. 적극적인 대처가 필요합니다.";
            riskLevel = "HIGH_RISK";
            suggestions = "스트레스로 인해 어려움을 겪고 있다면, 전문가와 상담을 고려해보세요.";
        }

        // 4. 결과를 DB에 저장합니다.
        ScaleAnalysisResultEntity scaleResultEntity = ScaleAnalysisResultEntity.builder()
                .userId(userId)
                .testType("PSYCHOLOGICAL_SCALE") // 대분류
                .testCategory("STRESS_SCALE")   // 소분류
                .totalScore((double) totalScore)
                .interpretation(interpretationText)
                .riskLevel(riskLevel)
                .suggestions(suggestions)
                .createdAt(LocalDateTime.now())
                .build();

        scaleAnalysisResultRepository.save(scaleResultEntity);

        // 5. 저장된 결과를 DTO로 변환하여 반환합니다.
        return scaleResultEntity.toDto();
    }
}