package com.test.seems.user.model.service;

import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import com.test.seems.counseling.jpa.repository.CounselingSessionRepository;
import com.test.seems.emotion.jpa.entity.EmotionLog;
import com.test.seems.emotion.jpa.repository.EmotionLogRepository;
import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.faq.jpa.repository.FaqRepository;
import com.test.seems.quest.jpa.entity.QuestEntity;
import com.test.seems.quest.jpa.repository.QuestRepository;
import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import com.test.seems.test.jpa.entity.ScaleAnalysisResultEntity;
import com.test.seems.test.jpa.repository.PersonalityTestResultRepository;
import com.test.seems.test.jpa.repository.PsychologicalTestResultRepository;
import com.test.seems.test.jpa.repository.ScaleAnalysisResultRepository;
import com.test.seems.user.model.dto.ActivityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

    // ✨ [수정] 의존성 주입을 통합된 리포지토리로 변경
    private final CounselingSessionRepository counselingSessionRepository;
    private final PersonalityTestResultRepository personalityTestResultRepository;
    private final PsychologicalTestResultRepository psychologicalTestResultRepository;
    private final ScaleAnalysisResultRepository scaleAnalysisResultRepository;
    private final QuestRepository questRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final FaqRepository faqRepository;
    
    /**
     * Date를 LocalDateTime으로 변환하는 유틸리티 메서드
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * Timestamp를 LocalDateTime으로 변환하는 유틸리티 메서드
     */
    private LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
    
    /**
     * 사용자 최근활동 조회
     */
    public List<ActivityDto> getRecentActivities(String userId, int limit) {
        List<ActivityDto> allActivities = new ArrayList<>();
        
        try {
            // 1. 상담 활동 조회
            allActivities.addAll(getCounselingActivities(userId));
            
            // 2. 심리검사 활동 조회
            allActivities.addAll(getPersonalityTestActivities(userId));
            allActivities.addAll(getPsychologicalTestActivities(userId));
            
            // 3. 퀘스트 활동 조회
            allActivities.addAll(getQuestActivities(userId));
            
            // 4. 감정 기록 활동 조회
            allActivities.addAll(getEmotionLogActivities(userId));
            
            // 5. FAQ 작성 활동 조회
            allActivities.addAll(getFaqActivities(userId));
            
            // 날짜순으로 정렬하고 limit만큼 반환
            return allActivities.stream()
                    .sorted(Comparator.comparing(ActivityDto::getActivityDate).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("최근활동 조회 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("최근활동 조회에 실패했습니다.", e);
        }
    }
    
    /**
     * 상담 활동 조회 (세션 완료 시점만)
     */
    private List<ActivityDto> getCounselingActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        
        try {
            List<CounselingSessionEntity> sessions = counselingSessionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            
            for (CounselingSessionEntity session : sessions) {
                activities.add(ActivityDto.builder()
                        .activityType("COUNSELING")
                        .title("상담 완료") // 구체적인 주제 대신 포괄적인 제목
                        .description("심리 상담 세션 완료") // 메시지 대신 활동 설명
                        .activityDate(convertToLocalDateTime(session.getCreatedAt()))
                        .status("완료")
                        .icon("fas fa-comments")
                        .color("primary")
                        .build());
            }
        } catch (Exception e) {
            log.warn("상담 활동 조회 실패: userId={}", userId, e);
        }
        
        return activities;
    }

    /**
     * 성격검사 활동 조회
     */
    private List<ActivityDto> getPersonalityTestActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        try {
            // ✨ [수정] 통합된 리포지토리(personalityTestResultRepository)를 사용하도록 변경
            List<PersonalityTestResultEntity> results = personalityTestResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
            for (PersonalityTestResultEntity result : results) {
                activities.add(ActivityDto.builder()
                        .activityType("PERSONALITY_TEST")
                        .title("성격검사 완료")
                        .description("MBTI 성격유형 검사")
                        .activityDate(result.getCreatedAt())
                        .status("완료")
                        .icon("fas fa-user-tie")
                        .color("info")
                        .build());
            }
        } catch (Exception e) {
            log.warn("성격검사 활동 조회 실패: userId={}", userId, e);
        }
        return activities;
    }

    /**
     * 심리검사 활동 조회
     */
    private List<ActivityDto> getPsychologicalTestActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        try {
            // 이미지 기반 심리검사
            // ✨ [수정] 통합된 리포지토리(psychologicalTestResultRepository)를 사용하도록 변경
            List<PsychologicalTestResultEntity> imageResults = psychologicalTestResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
            for (PsychologicalTestResultEntity result : imageResults) {
                activities.add(ActivityDto.builder()
                        .activityType("PSYCHOLOGICAL_TEST")
                        .title("심리검사 완료")
                        .description("이미지 기반 심리검사")
                        .activityDate(result.getCreatedAt())
                        .status("완료")
                        .icon("fas fa-brain")
                        .color("warning")
                        .build());
            }

            // 척도 기반 심리검사
            // ✨ [수정] 통합된 리포지토리(scaleAnalysisResultRepository)와 엔티티(ScaleAnalysisResultEntity)를 사용하도록 변경
            List<ScaleAnalysisResultEntity> scaleResults = scaleAnalysisResultRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            for (ScaleAnalysisResultEntity result : scaleResults) {
                activities.add(ActivityDto.builder()
                        .activityType("PSYCHOLOGICAL_TEST")
                        .title("심리검사 완료")
                        .description("척도 기반 심리검사")
                        // ✨ [수정 전]
                        // .activityDate(convertToLocalDateTime(result.getCreatedAt()))
                        // ✨ [수정 후] 불필요한 메서드 호출 삭제
                        .activityDate(result.getCreatedAt())
                        .status("완료")
                        .icon("fas fa-brain")
                        .color("warning")
                        .build());
            }
        } catch (Exception e) {
            log.warn("심리검사 활동 조회 실패: userId={}", userId, e);
        }
        return activities;
    }
    
    /**
     * 퀘스트 활동 조회
     */
    private List<ActivityDto> getQuestActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        
        try {
            List<QuestEntity> quests = questRepository.findByUserIdAndIsCompletedOrderByCreatedAtDesc(userId, 1);
            
            for (QuestEntity quest : quests) {
                activities.add(ActivityDto.builder()
                        .activityType("QUEST")
                        .title("퀘스트 완료") // 구체적인 퀘스트명 대신 포괄적인 제목
                        .description("퀘스트 완료 - " + quest.getQuestPoints() + "포인트 획득")
                        .activityDate(quest.getCreatedAt())
                        .status("완료")
                        .icon("fas fa-trophy")
                        .color("success")
                        .build());
            }
        } catch (Exception e) {
            log.warn("퀘스트 활동 조회 실패: userId={}", userId, e);
        }
        
        return activities;
    }
    
    /**
     * 감정 기록 활동 조회
     */
    private List<ActivityDto> getEmotionLogActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        
        try {
            List<EmotionLog> emotionLogs = emotionLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            for (EmotionLog emotionLog : emotionLogs) {
                activities.add(ActivityDto.builder()
                        .activityType("EMOTION_LOG")
                        .title("감정 기록")
                        .description("감정 상태 기록")
                        .activityDate(convertToLocalDateTime(emotionLog.getCreatedAt()))
                        .status("완료")
                        .icon("fas fa-heart")
                        .color("danger")
                        .build());
            }
        } catch (Exception e) {
            log.warn("감정 기록 활동 조회 실패: userId={}", userId, e);
        }
        
        return activities;
    }
    
    /**
     * FAQ 작성 활동 조회
     */
    private List<ActivityDto> getFaqActivities(String userId) {
        List<ActivityDto> activities = new ArrayList<>();
        
        try {
            List<FaqEntity> faqs = faqRepository.findByUseridOrderByFaqDateDesc(userId);
            
            for (FaqEntity faq : faqs) {
                activities.add(ActivityDto.builder()
                        .activityType("FAQ")
                        .title("문의사항 작성") // 구체적인 제목 대신 포괄적인 활동명
                        .description("문의사항 작성 - " + faq.getStatus()) // 구체적인 내용 대신 활동 설명
                        .activityDate(convertToLocalDateTime(faq.getFaqDate()))
                        .status(faq.getStatus())
                        .icon("fas fa-question-circle")
                        .color("dark")
                        .build());
            }
        } catch (Exception e) {
            log.warn("FAQ 활동 조회 실패: userId={}", userId, e);
        }
        
        return activities;
    }
} 