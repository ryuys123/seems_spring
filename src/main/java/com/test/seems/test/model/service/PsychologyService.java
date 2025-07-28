package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.*;
import com.test.seems.test.jpa.repository.*;
import com.test.seems.test.model.dto.*;
import com.test.seems.user.exception.UserNotFoundException;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
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
    private final PsychologicalImageAnswerRepository psychologicalImageAnswerRepository;
    private final PsychologicalImageResultRepository psychologicalImageResultRepository;
    private final PsychologicalScaleAnswerRepository psychologicalScaleAnswerRepository;
    private final PsychologicalScaleResultRepository psychologicalScaleResultRepository;
    private final UserRepository userRepository;
    private final WebClient pythonAiWebClient;

    @Autowired
    public PsychologyService(CommonQuestionRepository commonQuestionRepository,
                             PsychologicalImageAnswerRepository psychologicalImageAnswerRepository,
                             PsychologicalImageResultRepository psychologicalImageResultRepository,
                             PsychologicalScaleAnswerRepository psychologicalScaleAnswerRepository,
                             PsychologicalScaleResultRepository psychologicalScaleResultRepository,
                             UserRepository userRepository,
                             WebClient pythonAiWebClient) {
        this.commonQuestionRepository = commonQuestionRepository;
        this.psychologicalImageAnswerRepository = psychologicalImageAnswerRepository;
        this.psychologicalImageResultRepository = psychologicalImageResultRepository;
        this.psychologicalScaleAnswerRepository = psychologicalScaleAnswerRepository;
        this.psychologicalScaleResultRepository = psychologicalScaleResultRepository;
        this.userRepository = userRepository;
        this.pythonAiWebClient = pythonAiWebClient;
    }

    @Transactional(readOnly = true)
    public List<TestQuestion> getMultipleRandomQuestionsByType(int count, String testTypeOrCategory) {
        log.info("Fetching questions for testTypeOrCategory: {}", testTypeOrCategory);
        List<TestQuestionEntity> questions;

        if ("PSYCHOLOGICAL_IMAGE".equalsIgnoreCase(testTypeOrCategory)) {
            // 이미지 검사의 경우, testTypeOrCategory는 실제 TEST_TYPE
            questions = commonQuestionRepository.findByTestType(testTypeOrCategory);
        } else if ("DEPRESSION_SCALE".equalsIgnoreCase(testTypeOrCategory) || "STRESS_SCALE".equalsIgnoreCase(testTypeOrCategory)) {
            // 척도 검사의 경우, testTypeOrCategory는 CATEGORY이고, TEST_TYPE은 PSYCHOLOGICAL_SCALE
            questions = commonQuestionRepository.findByTestTypeAndCategory("PSYCHOLOGICAL_SCALE", testTypeOrCategory);
        } else {
            // 알 수 없는 유형에 대한 처리
            log.warn("Unknown testTypeOrCategory: {}", testTypeOrCategory);
            return Collections.emptyList();
        }

        log.info("Found {} questions for testTypeOrCategory {}: {}", questions.size(), testTypeOrCategory, questions);

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(questions, new Random());

        int limit = Math.min(count, questions.size());
        return questions.stream()
                .limit(limit)
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestQuestion> getQuestionsByTestTypeAndCategory(String category) {
        List<TestQuestionEntity> questions = commonQuestionRepository.findByCategory(category);
        return questions.stream()
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestQuestion getRandomImageQuestion() {
        List<TestQuestionEntity> imageBasedQuestions = commonQuestionRepository.findByTestTypeAndCategory("PSYCHOLOGICAL_IMAGE", "IMAGE_BASED");

        if (imageBasedQuestions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        TestQuestionEntity randomQuestionEntity = imageBasedQuestions.get(random.nextInt(imageBasedQuestions.size()));

        return randomQuestionEntity.toDto();
    }

    @Transactional
    public PsychologicalTestResultResponse submitPsychologicalAnswerSequentially(PsychologicalAnswerRequest answerRequest) {
        PsychologyEntity answerEntity = PsychologyEntity.builder()
                .userId(answerRequest.getUserId())
                .questionId(answerRequest.getQuestionId())
                .userResponseText(answerRequest.getUserResponseText())
                .testType(answerRequest.getTestType())
                .answerDatetime(LocalDateTime.now())
                .build();

        psychologicalImageAnswerRepository.save(answerEntity);

        int currentStep = answerRequest.getCurrentStep();
        int totalSteps = answerRequest.getTotalSteps();

        if (currentStep < totalSteps) {
            return null;
        }

        String userId = answerRequest.getUserId();

        Pageable pageable = PageRequest.of(0, totalSteps, Sort.by(Sort.Direction.DESC, "answerDatetime"));
        List<PsychologyEntity> recentAnswers = psychologicalImageAnswerRepository.findByUserIdOrderByAnswerDatetimeDesc(userId, pageable);

        Collections.reverse(recentAnswers);

        List<Map<String, Object>> userResponsesForAi = recentAnswers.stream()
                .map(answer -> Map.<String, Object>of(
                        "questionId", answer.getQuestionId(),
                        "userResponseText", answer.getUserResponseText()
                ))
                .collect(Collectors.toList());

        Map<String, Object> aiRequestBody = Map.of(
                "userId", userId,
                "responses", userResponsesForAi
        );

        log.info("AI 서버로 전송할 aiRequestBody: {}", aiRequestBody);

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
            aiResponseDto = PsychologicalTestResultResponse.builder()
                    .userId(userId)
                    .questionId(recentAnswers.get(0).getQuestionId())
                    .rawResponseText(recentAnswers.stream().map(PsychologyEntity::getUserResponseText).collect(Collectors.joining("\n---\n")))
                    .aiSentiment("분석불가")
                    .aiCreativityScore(0.0)
                    .aiPerspectiveKeywords("오류")
                    .aiInsightSummary("AI 분석 서버와 통신할 수 없습니다. (오류: " + e.getMessage() + "). AI 서버가 정상적으로 작동하는지 확인해주세요.")
                    .suggestions("AI 서버와 연결에 문제가 발생했습니다. 다시 시도하거나 관리자에게 문의하세요.")
                    .testDateTime(LocalDateTime.now())
                    .build();
        }

        PsychologicalTestResultEntity resultEntity = PsychologicalTestResultEntity.builder()
                .userId(aiResponseDto.getUserId())
                .questionId(aiResponseDto.getQuestionId())
                .rawResponseText(aiResponseDto.getRawResponseText())
                .testType("PSYCHOLOGICAL_IMAGE") // <--- 이 부분을 추가합니다.
                .aiSentiment(aiResponseDto.getAiSentiment())
                .aiSentimentScore(aiResponseDto.getAiSentimentScore())
                .aiCreativityScore(aiResponseDto.getAiCreativityScore())
                .aiPerspectiveKeywords(aiResponseDto.getAiPerspectiveKeywords())
                .aiInsightSummary(aiResponseDto.getAiInsightSummary())
                .suggestions(aiResponseDto.getSuggestions())
                .createdAt(LocalDateTime.now())
                .build();

        psychologicalImageResultRepository.save(resultEntity);

        return resultEntity.toDto();
    }

    @Transactional
    public PsychologicalScaleResult saveScaleTestResult(ScaleTestSubmissionDto submissionDto) {
        UserEntity user = userRepository.findById(submissionDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + submissionDto.getUserId()));

        for (ScaleAnswerDto answerDto : submissionDto.getAnswers()) {
            PsychologicalScaleAnswer answer = new PsychologicalScaleAnswer();
            answer.setUser(user);
            answer.setQuestionId(answerDto.getQuestionId());
            answer.setAnswerValue(answerDto.getAnswerValue());
            answer.setTestCategory(submissionDto.getTestCategory());
            answer.setTestType("PSYCHOLOGICAL_SCALE");
            psychologicalScaleAnswerRepository.save(answer);
        }

        double totalScore = submissionDto.getAnswers().stream()
                .mapToDouble(ScaleAnswerDto::getAnswerValue)
                .sum();

        String interpretation;
        String riskLevel;
        String suggestions;

        if ("DEPRESSION_SCALE".equalsIgnoreCase(submissionDto.getTestCategory())) {
            if (totalScore <= 4) {
                interpretation = "현재 우울감 수준이 정상 범위입니다. 건강한 상태를 유지하고 계십니다.";
                riskLevel = "양호";
                suggestions = "현재의 긍정적인 상태를 잘 유지하세요.";
            } else if (totalScore <= 9) {
                interpretation = "경미한 우울감이 감지됩니다. 가벼운 스트레스나 기분 변화일 수 있습니다.";
                riskLevel = "가벼운 수준";
                suggestions = "휴식, 취미 활동, 가벼운 운동 등을 통해 기분 전환을 시도해보세요.";
            } else if (totalScore <= 14) {
                interpretation = "중간 정도의 우울감이 나타납니다. 일상생활에 영향을 줄 수 있습니다.";
                riskLevel = "중간 수준";
                suggestions = "심리 상담 전문가와 이야기를 나눠보거나, 규칙적인 생활과 충분한 수면을 취하는 것이 중요합니다.";
            } else if (totalScore <= 19) {
                interpretation = "중증도의 우울감이 감지됩니다. 전문가의 도움이 필요할 수 있습니다.";
                riskLevel = "다소 심한 수준";
                suggestions = "반드시 정신건강의학과 전문의 또는 심리 상담사와 상담을 진행해 보세요. 주변의 지지체계를 활용하는 것도 좋습니다.";
            } else {
                interpretation = "심한 우울감이 나타나고 있습니다. 즉각적인 전문가의 도움이 필요합니다.";
                riskLevel = "심각한 수준";
                suggestions = "지체하지 말고 즉시 정신건강의학과 전문의와 상담을 시작하고 필요한 치료를 받으세요. 가족이나 친구에게 도움을 요청하세요.";
            }
        } else if ("STRESS_SCALE".equalsIgnoreCase(submissionDto.getTestCategory())) {
            if (totalScore <= 13) {
                interpretation = "스트레스 수준이 낮은 편입니다. 스트레스 관리를 잘 하고 계십니다.";
                riskLevel = "정상";
                suggestions = "현재의 긍정적인 상태를 잘 유지하고, 스트레스 해소 활동을 꾸준히 해보세요.";
            } else if (totalScore <= 26) {
                interpretation = "중간 정도의 스트레스 수준을 보입니다. 일상에서 스트레스 요인을 관리할 필요가 있습니다.";
                riskLevel = "약간의 스트레스";
                suggestions = "스트레스 원인을 파악하고, 명상, 취미, 휴식 등 자신에게 맞는 해소법을 찾아 실천해보세요.";
            } else {
                interpretation = "높은 수준의 스트레스를 경험하고 있습니다. 적극적인 대처가 필요합니다.";
                riskLevel = "높은 스트레스";
                suggestions = "스트레스로 인해 어려움을 겪고 있다면, 전문가(심리 상담사, 정신과 의사)와 상담을 고려해보세요. 규칙적인 생활과 충분한 수면도 중요합니다.";
            }
        } else {
            interpretation = "결과에 대한 해석이 준비되지 않았습니다.";
            riskLevel = "UNKNOWN";
            suggestions = "관리자에게 문의하세요.";
        }

        PsychologicalScaleResult result = new PsychologicalScaleResult();
        result.setUser(user);
        result.setTestCategory(submissionDto.getTestCategory());
        result.setTotalScore(totalScore);
        result.setInterpretation(interpretation);
        result.setRiskLevel(riskLevel);
        result.setSuggestions(suggestions);
        result.setTestType("PSYCHOLOGICAL_SCALE");

        return psychologicalScaleResultRepository.save(result);
    }

    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getPsychologicalTestResult(Long resultId, String testType) {
        if (testType == null || testType.trim().isEmpty()) {
            log.warn("getPsychologicalTestResult 호출 시 testType이 누락되었습니다. resultId: {}", resultId);
            throw new IllegalArgumentException("검사 유형(testType)은 필수입니다.");
        }

        switch (testType) {
            case "IMAGE_TEST":
            case "PSYCHOLOGICAL_IMAGE":
                return psychologicalImageResultRepository.findById(resultId)
                        .map(PsychologicalTestResultEntity::toDto)
                        .orElseThrow(() -> new RuntimeException("이미지 심리 검사 결과를 찾을 수 없습니다. (ID: " + resultId + ")"));

            case "DEPRESSION_SCALE":
            case "PSYCHOLOGICAL_SCALE":
            case "STRESS_SCALE":
                return psychologicalScaleResultRepository.findById(resultId)
                        .map(PsychologicalScaleResult::toDto)
                        .orElseThrow(() -> new RuntimeException(testType + " 척도 검사 결과를 찾을 수 없습니다. (ID: " + resultId + ")"));

            default:
                log.warn("알 수 없는 testType: {} (resultId: {})", testType, resultId);
                throw new IllegalArgumentException("알 수 없는 검사 유형입니다: " + testType);
        }
    }

    @Transactional(readOnly = true)
    public PsychologicalTestResultResponse getLatestPsychologicalTestResultByUserId(String userId) {
        Optional<PsychologicalTestResultEntity> latestImageResult = psychologicalImageResultRepository.findTop1ByUserIdOrderByCreatedAtDesc(userId);
        Optional<PsychologicalScaleResult> latestScaleResult = psychologicalScaleResultRepository.findTopByUser_UserIdOrderByCreatedAtDesc(userId);

        PsychologicalTestResultResponse result = null;

        if (latestImageResult.isPresent() && latestScaleResult.isPresent()) {
            if (latestImageResult.get().getCreatedAt().isAfter(latestScaleResult.get().getCreatedAt().toLocalDateTime())) {
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

    @Transactional(readOnly = true)
    public Optional<PsychologicalTestResultResponse> getLatestPsychologicalImageResult(String userId) {
        return psychologicalImageResultRepository.findTop1ByUserIdOrderByCreatedAtDesc(userId)
                .map(PsychologicalTestResultEntity::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<PsychologicalTestResultResponse> getLatestScaleResult(String userId, String testCategory) {
        return psychologicalScaleResultRepository.findTopByUser_UserIdAndTestCategoryOrderByCreatedAtDesc(userId, testCategory)
                .map(PsychologicalScaleResult::toDto);
    }
}
