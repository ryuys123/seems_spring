package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PersonalityEntity;
import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.repository.CommonQuestionRepository;
import com.test.seems.test.jpa.repository.PersonalityAnswerRepository;
import com.test.seems.test.jpa.repository.PersonalityTestResultRepository;
import com.test.seems.test.model.dto.PersonalityAnswer;
import com.test.seems.test.model.dto.PersonalitySubmission;
import com.test.seems.test.model.dto.PersonalityTestResult;
import com.test.seems.test.model.dto.TestQuestion;
import com.test.seems.user.exception.UserNotFoundException;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalityService {

    private final CommonQuestionRepository commonQuestionRepository;
    private final PersonalityAnswerRepository personalityAnswerRepository;
    // ✨ [수정] PersonalityResultRepository -> PersonalityTestResultRepository
    private final PersonalityTestResultRepository personalityTestResultRepository;
    private final UserRepository userRepository;

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
        return commonQuestionRepository.findByTestType("PERSONALITY").stream()
                .map(TestQuestionEntity::toDto)
                .collect(Collectors.toList());
    }

    // ✨ [수정] submitPersonalityTest 메서드 내부
    @Transactional
    public PersonalityTestResult submitPersonalityTest(PersonalitySubmission submissionDto) {
        UserEntity user = userRepository.findById(submissionDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + submissionDto.getUserId()));

        // 1. Save answers
        for (PersonalityAnswer answerDto : submissionDto.getAnswers()) {
            PersonalityEntity answerEntity = new PersonalityEntity();
            answerEntity.setUserId(user.getUserId());
            answerEntity.setQuestionId(answerDto.getQuestionId());
            answerEntity.setAnswerValue(String.valueOf(answerDto.getAnswerValue()));
            personalityAnswerRepository.save(answerEntity);
        }

        // 2. Calculate scores
        Map<String, Integer> scores = new HashMap<>();
        scores.put("E", 0); scores.put("I", 0);
        scores.put("S", 0); scores.put("N", 0);
        scores.put("T", 0); scores.put("F", 0);
        scores.put("J", 0); scores.put("P", 0);

        for (PersonalityAnswer answer : submissionDto.getAnswers()) {
            log.info("Processing answer for questionId: {}, answerValue: {}, scoreDirection: {}", answer.getQuestionId(), answer.getAnswerValue(), answer.getScoreDirection());
            // Assuming answerValue is 1-5 scale. 1,2 -> +1 for I,S,F,P. 4,5 -> +1 for E,N,T,J
            int value = answer.getAnswerValue();
            String direction = answer.getScoreDirection();

            if (direction == null || direction.isEmpty()) {
                log.warn("scoreDirection is null or empty for questionId: {}", answer.getQuestionId());
                continue; // Skip this answer or handle as an error
            }

            if (value < 3) { // Leans towards I, S, F, P
                String oppositeDirection = getOppositeDirection(direction);
                if (scores.containsKey(oppositeDirection)) {
                    scores.put(oppositeDirection, scores.get(oppositeDirection) + (3 - value));
                } else {
                    log.warn("Opposite direction {} not found in scores map for direction {}", oppositeDirection, direction);
                }
            } else if (value > 3) { // Leans towards E, N, T, J
                if (scores.containsKey(direction)) {
                    scores.put(direction, scores.get(direction) + (value - 3));
                } else {
                    log.warn("Direction {} not found in scores map", direction);
                }
            }
        }
        log.info("Calculated scores: {}", scores);

        // 3. Determine MBTI type
        String mbtiType = "";
        mbtiType += (scores.get("E") >= scores.get("I")) ? "E" : "I";
        mbtiType += (scores.get("S") >= scores.get("N")) ? "S" : "N";
        mbtiType += (scores.get("T") >= scores.get("F")) ? "T" : "F";
        mbtiType += (scores.get("J") >= scores.get("P")) ? "J" : "P";

        // 4. Save result
        PersonalityTestResultEntity resultEntity = new PersonalityTestResultEntity();
        resultEntity.setUserId(user.getUserId());
        resultEntity.setPersonalityTestId(1L); // Assuming 1L is the ID for MBTI test
        resultEntity.setResult(mbtiType);
        resultEntity.setDescription(mbtiDescriptions.get(mbtiType));
        resultEntity.setMbtiTitle(mbtiTitles.get(mbtiType));
        resultEntity.setCreatedAt(LocalDateTime.now());

        PersonalityTestResultEntity savedResult = personalityTestResultRepository.save(resultEntity);

        return new PersonalityTestResult(
                savedResult.getResult(),
                savedResult.getDescription(),
                savedResult.getMbtiTitle(),
                savedResult.getCreatedAt()
        );
    }

    private String getOppositeDirection(String direction) {
        switch (direction) {
            case "E": return "I";
            case "I": return "E";
            case "S": return "N";
            case "N": return "S";
            case "T": return "F";
            case "F": return "T";
            case "J": return "P";
            case "P": return "J";
            default: return "";
        }
    }

    // ✨ [수정] getPersonalityTestResult 메서드
    @Transactional(readOnly = true)
    public Optional<PersonalityTestResult> getPersonalityTestResult(String userId) {
        return personalityTestResultRepository // ✨ 변수명 수정
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(entity -> new PersonalityTestResult(
                        entity.getResult(),
                        entity.getDescription(),
                        entity.getMbtiTitle(),
                        entity.getCreatedAt()
                ));
    }

    // ✨ [수정] getTestHistoryByUserId 메서드
    @Transactional(readOnly = true)
    public List<PersonalityTestResult> getTestHistoryByUserId(String userId) {
        // 'findAllByUserIdOrderByCreatedAtDesc'는 우리가 통합한 리포지토리에 없는 메서드입니다.
        // 'findByUserIdOrderByCreatedAtDesc'로 변경합니다.
        return personalityTestResultRepository // ✨ 변수명 수정
                .findByUserIdOrderByCreatedAtDesc(userId) // ✨ 메서드명 수정
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
