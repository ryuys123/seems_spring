package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.repository.PersonalityRepository;
import com.test.seems.test.jpa.repository.PersonalityTestResultRepository;
import com.test.seems.test.jpa.repository.TestQuestionRepository;
import com.test.seems.test.model.dto.Personality;
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalityService {

    private final TestQuestionRepository testQuestionRepository;
    private final PersonalityRepository personalityRepository;
    private final PersonalityTestResultRepository personalityTestResultRepository;

    // ✨ 1. MBTI 유형별 칭호 목록을 Map으로 정의합니다.
    private static final Map<String, String> mbtiTitles = new HashMap<>();
    static {
        // 분석가형 (NT)
        mbtiTitles.put("INTJ", "모든 것을 꿰뚫어 보는, 통찰의 설계자");
        mbtiTitles.put("INTP", "논리의 미궁을 탐험하는, 지식의 탐구자");
        mbtiTitles.put("ENTJ", "세상을 움직이는, 담대한 지도자");
        mbtiTitles.put("ENTP", "끊임없이 질문하는, 지적인 전략가");
        // 외교관형 (NF)
        mbtiTitles.put("INFJ", "세상을 밝히는, 고요한 이상주의자");
        mbtiTitles.put("INFP", "따뜻한 마음을 지닌, 열정의 중재자");
        mbtiTitles.put("ENFJ", "사람들을 이끄는, 정의로운 웅변가");
        mbtiTitles.put("ENFP", "새로운 가능성을 찾는, 재기발랄한 활동가");
        // 관리자형 (SJ)
        mbtiTitles.put("ISTJ", "세상의 원칙을 세우는, 청렴한 현실주의자");
        mbtiTitles.put("ISFJ", "묵묵히 세상을 지키는, 용감한 수호자");
        mbtiTitles.put("ESTJ", "사회를 조직하는, 엄격한 관리자");
        mbtiTitles.put("ESFJ", "세상에 온기를 더하는, 사교적인 외교관");
        // 탐험가형 (SP)
        mbtiTitles.put("ISTP", "만능 재주꾼, 냉철한 해결사");
        mbtiTitles.put("ISFP", "세상의 아름다움을 그리는, 호기심 많은 예술가");
        mbtiTitles.put("ESTP", "세상을 무대로 삼는, 대담한 모험가");
        mbtiTitles.put("ESFP", "세상을 즐겁게 하는, 자유로운 영혼의 소유자");
    }

    @Transactional(readOnly = true)
    public List<TestQuestion> getPersonalityQuestions() {
        return testQuestionRepository.findByTestType("PERSONALITY").stream()
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalityTestResult submitAnswersAndCalculateResult(List<Personality> userAnswers) {
        String userId = userAnswers.get(0).getUserId();

        List<Long> questionIds = userAnswers.stream()
                .map(Personality::getQuestionId)
                .collect(Collectors.toList());

        Map<Long, TestQuestionEntity> questionMap = testQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(TestQuestionEntity::getQuestionId, q -> q));

        Map<String, Integer> scores = new HashMap<>();
        scores.put("E", 0); scores.put("I", 0);
        scores.put("S", 0); scores.put("N", 0);
        scores.put("T", 0); scores.put("F", 0);
        scores.put("J", 0); scores.put("P", 0);

        for (Personality answer : userAnswers) {
            TestQuestionEntity question = questionMap.get(answer.getQuestionId());
            if (question == null || question.getScoreDirection() == null) continue;

            int pointToAdd = answer.getAnswerValue() - 3;
            String direction = question.getScoreDirection();
            scores.put(direction, scores.get(direction) + pointToAdd);
        }

        String mbtiType = "";
        mbtiType += (scores.get("E") >= scores.get("I")) ? "E" : "I";
        mbtiType += (scores.get("S") >= scores.get("N")) ? "S" : "N";
        mbtiType += (scores.get("T") >= scores.get("F")) ? "T" : "F";
        mbtiType += (scores.get("J") >= scores.get("P")) ? "J" : "P";

        String description = "이것은 " + mbtiType + " 유형에 대한 기본 설명입니다.";

        // ✨ 2. 위에서 만든 맵을 사용해 mbtiType에 해당하는 칭호를 가져옵니다.
        String mbtiTitle = mbtiTitles.get(mbtiType);

        PersonalityTestResultEntity finalResultEntity = PersonalityTestResultEntity.builder()
                .userId(userId)
                .personalityTestId(1L)
                .result(mbtiType)
                .description(description)
                .mbtiTitle(mbtiTitle) // ✨ 이제 mbtiTitle 변수를 찾을 수 있습니다.
                .build();

        PersonalityTestResultEntity savedResult = personalityTestResultRepository.save(finalResultEntity);

        return new PersonalityTestResult(
                savedResult.getResult(),
                savedResult.getDescription(),
                savedResult.getMbtiTitle()
        );
    }

    @Transactional(readOnly = true)
    public PersonalityTestResult getPersonalityTestResult(String userId) {
        PersonalityTestResultEntity resultEntity = personalityTestResultRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 검사 결과를 찾을 수 없습니다. userId=" + userId));

        return new PersonalityTestResult(
                resultEntity.getResult(),
                resultEntity.getDescription(),
                resultEntity.getMbtiTitle()
        );
    }
}