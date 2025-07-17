// src/main/java/com/test/seems/test/model/service/PersonalityService.java
package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.repository.CommonQuestionRepository;
import com.test.seems.test.jpa.repository.PersonalityAnswerRepository;
import com.test.seems.test.jpa.repository.PersonalityResultRepository;
import com.test.seems.test.model.dto.PersonalityAnswerRequest;
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 성격 검사와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor // final 필드들을 주입받는 생성자를 자동으로 생성
public class PersonalityService {

    private final CommonQuestionRepository commonQuestionRepository; // ⭐ CommonQuestionRepository 주입
    private final PersonalityAnswerRepository personalityAnswerRepository;
    private final PersonalityResultRepository personalityResultRepository;

    // --- MBTI 유형별 칭호 및 설명 목록 (변동 없음) ---
    private static final Map<String, String> mbtiTitles = new HashMap<>();
    static {
        mbtiTitles.put("INTJ", "모든 것을 꿰뚫어 보는, 통찰의 설계자");
        mbtiTitles.put("INTP", "논리의 미궁을 탐험하는, 지식의 탐구자");
        mbtiTitles.put("ENTJ", "세상을 움직이는, 담대한 지도자");
        mbtiTitles.put("ENTP", "끊임없이 질문하는, 지적인 전략가");
        mbtiTitles.put("INFJ", "세상을 밝히는, 고요한 이상주의자");
        mbtiTitles.put("INFP", "따뜻한 마음을 지닌, 열정의 중재자");
        mbtiTitles.put("ENFJ", "사람들을 이끄는, 정의로운 웅변가");
        mbtiTitles.put("ENFP", "새로운 가능성을 찾는, 재기발랄한 활동가");
        mbtiTitles.put("ISTJ", "세상의 원칙을 세우는, 청렴한 현실주의자");
        mbtiTitles.put("ISFJ", "묵묵히 세상을 지키는, 용감한 수호자");
        mbtiTitles.put("ESTJ", "사회를 조직하는, 엄격한 관리자");
        mbtiTitles.put("ESFJ", "세상에 온기를 더하는, 사교적인 외교관");
        mbtiTitles.put("ISTP", "만능 재주꾼, 냉철한 해결사");
        mbtiTitles.put("ISFP", "세상의 아름다움을 그리는, 호기심 많은 예술가");
        mbtiTitles.put("ESTP", "세상을 무대로 삼는, 대담한 모험가");
        mbtiTitles.put("ESFP", "세상을 즐겁게 하는, 자유로운 영혼의 소유자");
    }

    private static final Map<String, String> mbtiDescriptions = new HashMap<>();
    static {
        mbtiDescriptions.put("INTJ", "상상력이 풍부하며 철두철미한 계획을 세우는 전략가입니다. 모든 일에 계획을 세우고, 상상 속에서 대화를 하거나 아이디어를 검토하는 것을 즐깁니다.");
        mbtiDescriptions.put("INTP", "끊임없이 새로운 지식에 목말라 하는 혁신가입니다. 독창적이고 지적인 호기심이 많아, 주변 사람들이 당연하게 생각하는 문제에 대해 다른 관점으로 접근하곤 합니다.");
        mbtiDescriptions.put("ENTJ", "대담하고 상상력이 풍부하며, 의지가 강한 지도자입니다. 문제를 해결하고 목표를 달성하는 과정에서 큰 만족감을 느끼며, 다른 사람들을 이끄는 데 능숙합니다.");
        mbtiDescriptions.put("ENTP", "지적인 도전을 두려워하지 않는 똑똑한 도전자입니다. 논쟁이나 브레인스토밍을 즐기며, 기존의 방식을 뒤엎는 새로운 아이디어를 제시하는 것을 좋아합니다.");
        mbtiDescriptions.put("INFJ", "조용하고 신비로우며, 다른 사람에게 의욕을 불어넣는 이상주의자입니다. 세상을 더 나은 곳으로 만들고자 하는 깊은 신념을 가지고 행동합니다.");
        mbtiDescriptions.put("INFP", "상냥하고 친절하며, 이타적인 성격을 가진 낭만적인 중재자입니다. 겉보기에는 조용해 보일 수 있지만, 내면에는 뜨거운 열정과 깊은 신념이 숨겨져 있습니다.");
        mbtiDescriptions.put("ENFJ", "카리스마와 설득력을 바탕으로 사람들을 이끄는 지도자입니다. 다른 사람의 성장을 돕는 것에서 큰 기쁨을 느끼며, 공동체의 목표를 위해 헌신합니다.");
        mbtiDescriptions.put("ENFP", "창의적이고 활발하며, 긍정적인 에너지가 넘치는 활동가입니다. 자유로운 영혼의 소유자로, 다른 사람들과 깊은 정서적 교감을 나누는 것을 중요하게 생각합니다.");
        mbtiDescriptions.put("ISTJ", "사실에 근거하여 사고하며, 현실적이고 책임감이 강한 사람입니다. 자신의 의무를 진지하게 생각하며, 한번 시작한 일은 끝까지 완수하는 신뢰할 수 있는 성격입니다.");
        mbtiDescriptions.put("ISFJ", "겸손하고 헌신적이며, 주변 사람들을 세심하게 챙기는 수호자입니다. 따뜻한 마음씨를 가졌으며, 다른 사람을 보호하고 돕는 데에서 큰 보람을 느낍니다.");
        mbtiDescriptions.put("ESTJ", "사물이나 사람을 관리하는 데 뛰어난 재능을 가진 현실적인 관리자입니다. 질서와 조직을 중시하며, 명확한 규칙에 따라 행동하는 것을 선호합니다.");
        mbtiDescriptions.put("ESFJ", "타인에게 깊은 관심을 가지고 있으며, 사교적이고 인기가 많은 사람입니다. 다른 사람을 돕는 것을 좋아하며, 주변 사람들이 행복할 때 자신도 행복을 느낍니다.");
        mbtiDescriptions.put("ISTP", "대담하고 현실적이며, 다양한 도구를 자유자재로 다루는 장인입니다. 호기심이 많아 직접 분해하고 조립하며 사물의 작동 원리를 파악하는 것을 즐깁니다.");
        mbtiDescriptions.put("ISFP", "따뜻하고 겸손하며, 새로운 것을 시도하고 탐험하는 것을 즐기는 예술가입니다. 현재의 순간을 소중히 여기며, 유연하고 즉흥적인 삶을 살아갑니다.");
        mbtiDescriptions.put("ESTP", "명석한 두뇌와 뛰어난 직관력을 가졌으며, 사람들과 어울리는 것을 즐기는 사업가입니다. 위험을 감수하는 것을 두려워하지 않으며, 삶을 흥미진진한 도전으로 가득 채웁니다.");
        mbtiDescriptions.put("ESFP", "즉흥적이고 에너지가 넘치며, 주변 사람들을 즐겁게 하는 타고난 연예인입니다. 스포트라이트를 즐기며, 일상 속에서 즐거움을 찾는 데 능숙합니다.");
    }

    @Transactional(readOnly = true)
    public List<TestQuestion> getPersonalityQuestions() {
        // commonQuestionRepository를 사용하여 'PERSONALITY' 타입의 질문을 가져옵니다.
        return commonQuestionRepository.findByTestType("PERSONALITY").stream()
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalityTestResult submitAnswersAndCalculateResult(List<PersonalityAnswerRequest> userAnswers) { // PersonalityAnswerRequest 사용
        String userId = userAnswers.get(0).getUserId();
        Long testId = userAnswers.get(0).getPersonalityTestId(); // PersonalityAnswerRequest에서 personalityTestId 가져옴

        // 1. 답변 저장 (TB_PERSONALITY_ANSWERS에 저장)
        for (PersonalityAnswerRequest answer : userAnswers) {
            // PersonalityAnswerRequest의 toEntity() 메서드를 사용하거나, 직접 엔티티 빌드
            personalityAnswerRepository.save(
                    com.test.seems.test.jpa.entity.PersonalityEntity.builder() // 엔티티 패키지명 포함
                            .userId(answer.getUserId())
                            .questionId(answer.getQuestionId())
                            .answerValue(answer.getAnswerValue()) // PersonalityEntity의 answerValue가 String이라면 그대로 사용
                            .build()
            );
        }

        List<Long> questionIds = userAnswers.stream().map(PersonalityAnswerRequest::getQuestionId).collect(Collectors.toList());
        Map<Long, TestQuestionEntity> questionMap = commonQuestionRepository.findAllById(questionIds)
                .stream().collect(Collectors.toMap(TestQuestionEntity::getQuestionId, q -> q));

        Map<String, Integer> scores = new HashMap<>();
        scores.put("E", 0); scores.put("I", 0);
        scores.put("S", 0); scores.put("N", 0);
        scores.put("T", 0); scores.put("F", 0);
        scores.put("J", 0); scores.put("P", 0);

        for (PersonalityAnswerRequest answer : userAnswers) {
            TestQuestionEntity question = questionMap.get(answer.getQuestionId());
            if (question == null || question.getScoreDirection() == null) continue;

            // ⭐ answer.getAnswerValue()가 String이므로 Integer.parseInt()로 변환 필요 ⭐
            // 또는, 답변이 숫자가 아니라 'YES/NO' 등의 문자열이라면 점수 계산 로직을 변경해야 합니다.
            // 여기서는 1~5점 척도 답변이라고 가정하고 숫자로 변환합니다.
            int answerIntValue;
            try {
                answerIntValue = Integer.parseInt(answer.getAnswerValue());
            } catch (NumberFormatException e) {
                log.error("MBTI 답변 값 파싱 오류: {}는 유효한 숫자가 아닙니다. userId: {}", answer.getAnswerValue(), userId);
                continue; // 유효하지 않은 답변은 건너뜀
            }

            int pointToAdd = answerIntValue - 3; // MBTI 계산 방식에 따라 점수 조정 (예: 1점: -2, 2점: -1, 3점: 0, 4점: 1, 5점: 2)
            String direction = question.getScoreDirection();
            scores.put(direction, scores.get(direction) + pointToAdd);
        }

        String mbtiType = "";
        mbtiType += (scores.get("E") >= scores.get("I")) ? "E" : "I";
        mbtiType += (scores.get("S") >= scores.get("N")) ? "S" : "N";
        mbtiType += (scores.get("T") >= scores.get("F")) ? "T" : "F";
        mbtiType += (scores.get("J") >= scores.get("P")) ? "J" : "P";

        String description = mbtiDescriptions.getOrDefault(mbtiType, "해당 유형에 대한 설명을 준비 중입니다.");
        String mbtiTitle = mbtiTitles.get(mbtiType);

        // 3. PersonalityTestResultEntity (TB_PERSONALITY_RESULTS)에 결과 저장
        PersonalityTestResultEntity finalResultEntity = PersonalityTestResultEntity.builder()
                .userId(userId)
                .personalityTestId(testId)
                .result(mbtiType)
                .description(description)
                .mbtiTitle(mbtiTitle)
                .createdAt(LocalDateTime.now()) // @CreationTimestamp를 사용하면 필요 없을 수 있음. 엔티티에 따라 조정.
                .build();

        PersonalityTestResultEntity savedResult = personalityResultRepository.save(finalResultEntity);

        // 4. PersonalityTestResult DTO로 변환하여 반환
        return new PersonalityTestResult(
                savedResult.getResult(),
                savedResult.getDescription(),
                savedResult.getMbtiTitle(),
                savedResult.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Optional<PersonalityTestResult> getPersonalityTestResult(String userId) {
        // personalityResultRepository를 사용하여 최신 성격 검사 결과 조회
        return personalityResultRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(entity -> new PersonalityTestResult(
                        entity.getResult(),
                        entity.getDescription(),
                        entity.getMbtiTitle(),
                        entity.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public List<PersonalityTestResult> getTestHistoryByUserId(String userId) {
        // personalityResultRepository를 사용하여 특정 사용자의 모든 성격 검사 기록 조회
        return personalityResultRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(entity -> new PersonalityTestResult(
                        entity.getResult(),
                        entity.getDescription(),
                        entity.getMbtiTitle(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}