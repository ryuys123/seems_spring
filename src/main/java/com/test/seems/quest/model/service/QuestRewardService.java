package com.test.seems.quest.model.service;

import com.test.seems.quest.exception.QuestException;
import com.test.seems.quest.jpa.entity.QuestRewardEntity;
import com.test.seems.quest.jpa.entity.UserPointsEntity;
import com.test.seems.quest.jpa.entity.UserRewardEntity;
import com.test.seems.quest.jpa.repository.QuestRewardRepository;
import com.test.seems.quest.jpa.repository.UserPointsRepository;
import com.test.seems.quest.jpa.repository.UserRewardRepository;
import com.test.seems.quest.jpa.repository.QuestRepository;
import com.test.seems.quest.model.dto.QuestRewardDto;
import com.test.seems.quest.model.dto.PurchaseRequestDto;
import com.test.seems.quest.model.dto.UserPointsDto;
import com.test.seems.quest.model.dto.UserStatsDto;
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
public class QuestRewardService {
    
    private final QuestRewardRepository questRewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final UserPointsRepository userPointsRepository;
    private final QuestRepository questRepository;
    
    /**
     * 모든 뱃지 보상 목록 조회
     */
    public List<QuestRewardDto> getAllQuestRewards() {
        List<QuestRewardEntity> entities = questRewardRepository.findAllByOrderByRequiredPointsAsc();
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자 포인트 조회
     */
    public UserPointsDto getUserPoints(String userId) {
        UserPointsEntity entity = userPointsRepository.findByUserId(userId)
                .orElse(UserPointsEntity.builder()
                        .userId(userId)
                        .points(0)
                        .build());
        
        return UserPointsDto.builder()
                .userId(entity.getUserId())
                .points(entity.getPoints())
                .build();
    }
    
    /**
     * 사용자 통계 조회
     */
    public UserStatsDto getUserStats(String userId) {
        // 보유한 뱃지 개수
        long ownedTitles = userRewardRepository.countByUserId(userId);
        
        // 전체 뱃지 개수
        long totalTitles = questRewardRepository.count();
        
        // 실제 퀘스트 통계 조회
        Long completedQuests = questRepository.countCompletedQuestsByUserId(userId);
        Long totalQuests = questRepository.countTotalQuestsByUserId(userId);
        
        return UserStatsDto.builder()
                .level(5) // 임시 레벨 (나중에 레벨 시스템 구현 시 수정)
                .completedQuests(completedQuests != null ? completedQuests.intValue() : 0)
                .totalQuests(totalQuests != null ? totalQuests.intValue() : 0)
                .ownedTitles((int) ownedTitles)
                .totalTitles((int) totalTitles)
                .build();
    }
    
    /**
     * 사용자가 보유한 뱃지 ID 목록 조회
     */
    public List<Long> getOwnedRewardIds(String userId) {
        return userRewardRepository.findRewardIdsByUserId(userId);
    }
    
    /**
     * 뱃지 구매
     */
    @Transactional
    public void purchaseReward(String userId, PurchaseRequestDto request) {
        // 뱃지 정보 조회
        QuestRewardEntity reward = questRewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new QuestException("존재하지 않는 뱃지입니다."));
        
        // 이미 보유한 뱃지인지 확인 (더 강력한 체크)
        boolean alreadyOwned = userRewardRepository.findByUserIdAndRewardId(userId, request.getRewardId()).isPresent();
        if (alreadyOwned) {
            log.warn("User {} already owns reward {}", userId, request.getRewardId());
            throw new QuestException("이미 보유한 뱃지입니다.");
        }
        
        // 사용자 포인트 조회
        UserPointsEntity userPoints = userPointsRepository.findByUserId(userId)
                .orElse(UserPointsEntity.builder()
                        .userId(userId)
                        .points(0)
                        .build());
        
        // 포인트가 부족한지 확인
        if (userPoints.getPoints() < reward.getRequiredPoints()) {
            throw new QuestException("포인트가 부족합니다. 필요: " + reward.getRequiredPoints() + ", 보유: " + userPoints.getPoints());
        }
        
        // 포인트 차감
        int updatedRows = userPointsRepository.deductPoints(userId, reward.getRequiredPoints());
        if (updatedRows == 0) {
            throw new QuestException("포인트 차감에 실패했습니다.");
        }
        
        try {
            // 뱃지 획득 기록
            UserRewardEntity userReward = UserRewardEntity.builder()
                    .userId(userId)
                    .rewardId(reward.getRewardId())
                    .acquiredAt(LocalDateTime.now())
                    .isApplied(0)
                    .build();
            
            userRewardRepository.save(userReward);
            
            log.info("User {} purchased reward {} for {} points", userId, reward.getRewardId(), reward.getRequiredPoints());
        } catch (Exception e) {
            log.error("Failed to save user reward for userId: {}, rewardId: {}", userId, request.getRewardId(), e);
            throw new QuestException("뱃지 획득 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
    
    /**
     * 퀘스트 상점 메인 페이지 데이터 조회
     */
    public QuestStoreDto getQuestStoreData(String userId) {
        UserPointsDto userPoints = getUserPoints(userId);
        UserStatsDto userStats = getUserStats(userId);
        List<QuestRewardDto> allRewards = getAllQuestRewards();
        List<Long> ownedRewardIds = getOwnedRewardIds(userId);
        
        return QuestStoreDto.builder()
                .userPoints(userPoints)
                .userStats(userStats)
                .allRewards(allRewards)
                .ownedRewardIds(ownedRewardIds)
                .build();
    }
    
    /**
     * 포인트 추가
     */
    @Transactional
    public void addPoints(String userId, Integer points) {
        if (points <= 0) {
            throw new QuestException("추가할 포인트는 0보다 커야 합니다.");
        }
        
        int updatedRows = userPointsRepository.addPoints(userId, points);
        if (updatedRows == 0) {
            // 사용자가 포인트 기록이 없는 경우 새로 생성
            UserPointsEntity newUserPoints = UserPointsEntity.builder()
                    .userId(userId)
                    .points(points)
                    .build();
            userPointsRepository.save(newUserPoints);
        }
        
        log.info("Points added - userId: {}, points: {}", userId, points);
    }
    
    /**
     * 포인트 차감
     */
    @Transactional
    public void deductPoints(String userId, Integer points) {
        if (points <= 0) {
            throw new QuestException("차감할 포인트는 0보다 커야 합니다.");
        }
        
        // 현재 포인트 확인
        UserPointsEntity userPoints = userPointsRepository.findByUserId(userId)
                .orElseThrow(() -> new QuestException("사용자의 포인트 정보를 찾을 수 없습니다."));
        
        if (userPoints.getPoints() < points) {
            throw new QuestException("보유 포인트가 부족합니다. 보유: " + userPoints.getPoints() + ", 차감: " + points);
        }
        
        int updatedRows = userPointsRepository.deductPoints(userId, points);
        if (updatedRows == 0) {
            throw new QuestException("포인트 차감에 실패했습니다.");
        }
        
        log.info("Points deducted - userId: {}, points: {}", userId, points);
    }
    
    /**
     * 포인트 업데이트 (통합)
     */
    @Transactional
    public void updatePoints(String userId, Integer points) {
        if (points < 0) {
            throw new QuestException("포인트는 0 이상이어야 합니다.");
        }
        
        UserPointsEntity userPoints = userPointsRepository.findByUserId(userId)
                .orElse(UserPointsEntity.builder()
                        .userId(userId)
                        .points(0)
                        .build());
        
        userPoints.setPoints(points);
        userPointsRepository.save(userPoints);
        
        log.info("Points updated - userId: {}, new points: {}", userId, points);
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private QuestRewardDto convertToDto(QuestRewardEntity entity) {
        return QuestRewardDto.builder()
                .rewardId(entity.getRewardId())
                .questName(entity.getQuestName())
                .requiredPoints(entity.getRequiredPoints())
                .rewardType(entity.getRewardRarity()) // rewardRarity를 rewardType으로 매핑
                .titleReward(entity.getTitleReward())
                .description(entity.getDescription())
                .imagePath(entity.getImagePath())
                .build();
    }
    
    /**
     * 퀘스트 상점 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class QuestStoreDto {
        private UserPointsDto userPoints;
        private UserStatsDto userStats;
        private List<QuestRewardDto> allRewards;
        private List<Long> ownedRewardIds;
    }
} 