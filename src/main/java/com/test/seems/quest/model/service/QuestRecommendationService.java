package com.test.seems.quest.model.service;

import com.test.seems.quest.jpa.entity.QuestRecommendationEntity;
import com.test.seems.quest.jpa.repository.QuestRecommendationRepository;
import com.test.seems.quest.model.dto.QuestRecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestRecommendationService {
    private final QuestRecommendationRepository questRecommendationRepository;

    public List<QuestRecommendationDto> getRecommendationsByEmotion(Long emotionId) {
        List<QuestRecommendationEntity> entities = questRecommendationRepository.findByEmotionId(emotionId);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<QuestRecommendationDto> getRecommendationsByUserId(String userId) {
        List<QuestRecommendationEntity> entities = questRecommendationRepository.findRandom6ByUserIdLatestEmotion(userId);
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    private QuestRecommendationDto toDto(QuestRecommendationEntity entity) {
        return QuestRecommendationDto.builder()
                .recommendId(entity.getRecommendId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .duration(entity.getDuration())
                .reward(entity.getReward())
                .build();
    }
} 