package com.test.seems.quest.model.service;

import com.test.seems.quest.exception.QuestException;
import com.test.seems.quest.jpa.entity.QuestEntity;
import com.test.seems.quest.jpa.repository.QuestRepository;
import com.test.seems.quest.jpa.repository.UserPointsRepository;
import com.test.seems.quest.model.dto.QuestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {
    
    private final QuestRepository questRepository;
    private final UserPointsRepository userPointsRepository;
    
    /**
     * 사용자의 모든 퀘스트 조회
     */
    public List<QuestDto> getUserQuests(String userId) {
        List<QuestEntity> quests = questRepository.findByUserId(userId);
        return quests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 완료된 퀘스트 조회
     */
    public List<QuestDto> getCompletedQuests(String userId) {
        List<QuestEntity> quests = questRepository.findByUserIdAndIsCompleted(userId, 1);
        return quests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 미완료 퀘스트 조회
     */
    public List<QuestDto> getIncompleteQuests(String userId) {
        List<QuestEntity> quests = questRepository.findByUserIdAndIsCompleted(userId, 0);
        return quests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 퀘스트 완료 처리
     */
    @Transactional
    public void completeQuest(String userId, Long questId) {
        QuestEntity quest = questRepository.findById(questId)
                .orElseThrow(() -> new QuestException("퀘스트를 찾을 수 없습니다."));
        
        if (!quest.getUserId().equals(userId)) {
            throw new QuestException("해당 퀘스트에 대한 권한이 없습니다.");
        }
        
        if (quest.getIsCompleted() == 1) {
            throw new QuestException("이미 완료된 퀘스트입니다.");
        }
        
        // 퀘스트 완료 처리
        quest.setIsCompleted(1);
        questRepository.save(quest);
        
        // 포인트 지급
        userPointsRepository.addPoints(userId, quest.getQuestPoints());
        
        log.info("Quest completed - userId: {}, questId: {}, points: {}", userId, questId, quest.getQuestPoints());
    }
    
    /**
     * 퀘스트 단계 완료 처리
     */
    @Transactional
    public void completeQuestStep(String userId, Long questId, String stepId, Boolean completed) {
        QuestEntity quest = questRepository.findById(questId)
                .orElseThrow(() -> new QuestException("퀘스트를 찾을 수 없습니다."));
        
        if (!quest.getUserId().equals(userId)) {
            throw new QuestException("해당 퀘스트에 대한 권한이 없습니다.");
        }
        
        // 현재는 단순히 퀘스트 완료 상태를 업데이트
        // 실제로는 TB_QUEST_STEPS 테이블이 필요하지만, 현재 구조에서는 퀘스트 자체를 완료 처리
        if (completed) {
            if (quest.getIsCompleted() == 0) {
                quest.setIsCompleted(1);
                questRepository.save(quest);
                
                // 포인트 지급
                userPointsRepository.addPoints(userId, quest.getQuestPoints());
                
                log.info("Quest step completed - userId: {}, questId: {}, stepId: {}, points: {}", 
                        userId, questId, stepId, quest.getQuestPoints());
            }
        } else {
            // 단계 미완료 처리 (퀘스트를 미완료로 변경)
            if (quest.getIsCompleted() == 1) {
                quest.setIsCompleted(0);
                questRepository.save(quest);
                
                log.info("Quest step uncompleted - userId: {}, questId: {}, stepId: {}", 
                        userId, questId, stepId);
            }
        }
    }
    
    /**
     * 새로운 퀘스트 생성
     */
    @Transactional
    public QuestDto createQuest(String userId, String questName, Integer questPoints) {
        QuestEntity quest = QuestEntity.builder()
                .userId(userId)
                .questName(questName)
                .questPoints(questPoints)
                .isCompleted(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        QuestEntity savedQuest = questRepository.save(quest);
        return convertToDto(savedQuest);
    }
    
    /**
     * 사용자의 퀘스트 통계 조회
     */
    public QuestStatsDto getQuestStats(String userId) {
        Long totalQuests = questRepository.countTotalQuestsByUserId(userId);
        Long completedQuests = questRepository.countCompletedQuestsByUserId(userId);
        
        return QuestStatsDto.builder()
                .totalQuests(totalQuests)
                .completedQuests(completedQuests)
                .completionRate(totalQuests > 0 ? (double) completedQuests / totalQuests * 100 : 0)
                .build();
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private QuestDto convertToDto(QuestEntity entity) {
        return QuestDto.builder()
                .questId(entity.getQuestId())
                .userId(entity.getUserId())
                .questName(entity.getQuestName())
                .questPoints(entity.getQuestPoints())
                .isCompleted(entity.getIsCompleted())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    /**
     * 퀘스트 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class QuestStatsDto {
        private Long totalQuests;
        private Long completedQuests;
        private Double completionRate;
    }
} 