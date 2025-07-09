package com.test.seems.test.model.service;

import com.test.seems.test.jpa.entity.PersonalityEntity;
import com.test.seems.test.jpa.entity.TestQuestionEntity;
import com.test.seems.test.jpa.repository.PersonalityRepository;
import com.test.seems.test.jpa.repository.TestQuestionRepository;
import com.test.seems.test.model.dto.Personality;
import com.test.seems.test.model.dto.TestQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalityService {

    private final TestQuestionRepository testQuestionRepository;
    private final PersonalityRepository personalityRepository;

    @Autowired
    public PersonalityService(TestQuestionRepository testQuestionRepository,
                              PersonalityRepository personalityRepository) {
        this.testQuestionRepository = testQuestionRepository;
        this.personalityRepository = personalityRepository;
    }

    @Transactional(readOnly = true)
    public List<TestQuestion> getPersonalityQuestions() {
        List<TestQuestionEntity> questions = testQuestionRepository.findByTestType("PERSONALITY");
        return questions.stream()
                .map(TestQuestionEntity::toDto) // <<-- 엔티티의 toDto() 메소드 호출
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitAnswersAndCalculateResult(List<Personality> answersRequest) {
        for (Personality dto : answersRequest) {
            PersonalityEntity entity = dto.toEntity(); // <<-- DTO의 toEntity() 메소드 호출
            personalityRepository.save(entity);
        }

        Long userId = answersRequest.get(0).getUserId();
        // MBTI 결과 계산 로직 (계산은 수행하나, 현재 결과는 반환하지 않음)
        // PersonalityTestResultResponseDto calculatedResult = calculateMbtiResult(userId, answersRequest);
        // System.out.println("MBTI 계산 완료 (현재 DB 저장 및 프론트엔드 반환은 미구현): " + calculatedResult.getMbtiType());
    }

    // 결과 조회 API는 현재 제외
    /*
    // @Transactional(readOnly = true)
    // public PersonalityTestResultResponseDto getLatestPersonalityTestResult(Long userId) {
    //     return null;
    // }
    */

    // --- 헬퍼 메서드: 엔티티-DTO 변환 (삭제) ---
    // convertToQuestionResponse 및 convertToAnswerEntity 메소드는 삭제됩니다.


    // MBTI 계산 로직 (계산은 수행하나, 현재 결과는 반환하지 않음)
//    private PersonalityTestResultResponseDto calculateMbtiResult(
//            Long userId, List<Personality> answersRequest) {
//        // ... (계산 로직 동일)
//
//        Map<String, Double> mbtiScores = new HashMap<>();
//        mbtiScores.put("E", 0.0); mbtiScores.put("I", 0.0);
//        mbtiScores.put("S", 0.0); mbtiScores.put("N", 0.0);
//        mbtiScores.put("T", 0.0); mbtiScores.put("F", 0.0);
//        mbtiScores.put("J", 0.0); mbtiScores.put("P", 0.0);
//
//        List<Long> questionIds = answersRequest.stream()
//                .map(Personality::getQuestionId)
//                .collect(Collectors.toList());
//        List<TestQuestionEntity> questionsInDb = testQuestionRepository.findAllById(questionIds);
//        Map<Long, TestQuestionEntity> questionMap = questionsInDb.stream()
//                .collect(Collectors.toMap(TestQuestionEntity::getQuestionId, q -> q));
//
//        for (Personality answerDto : answersRequest) {
//            TestQuestionEntity question = questionMap.get(answerDto.getQuestionId());
//
//            if (question == null || question.getScoreDirection() == null) {
//                System.err.println("경고: 문항 ID " + answerDto.getQuestionId() + "에 대한 정보가 불충분합니다. 스킵합니다.");
//                continue;
//            }
//
//            int userAnswerValue = answerDto.getAnswerValue();
//            double weight = question.getWeight() != null ? question.getWeight() : 1.0;
//
//            double scoreToAdd = (userAnswerValue - 3) * weight;
//
//            String direction = question.getScoreDirection();
//            mbtiScores.put(direction, mbtiScores.getOrDefault(direction, 0.0) + scoreToAdd);
//        }
//
//        String mbtiType = "";
//        String finalEI = (mbtiScores.get("E") >= mbtiScores.get("I")) ? "E" : "I";
//        String finalSN = (mbtiScores.get("S") >= mbtiScores.get("N")) ? "S" : "N";
//        String finalTF = (mbtiScores.get("T") >= mbtiScores.get("F")) ? "T" : "F";
//        String finalJP = (mbtiScores.get("J") >= mbtiScores.get("P")) ? "J" : "P";
//
//        mbtiType = finalEI + finalSN + finalTF + finalJP;
//
//        String description = "이것은 " + mbtiType + " 유형에 대한 설명입니다. (실제 설명 추가 필요)";
//
//        return new PersonalityTestResultResponseDto(
//                null,
//                mbtiType,
//                description,
//                mbtiScores.get("E"),
//                mbtiScores.get("I"),
//                mbtiScores.get("S"),
//                mbtiScores.get("N"),
//                mbtiScores.get("T"),
//                mbtiScores.get("F"),
//                mbtiScores.get("J"),
//                mbtiScores.get("P"),
//                LocalDateTime.now()
//        );
    }
