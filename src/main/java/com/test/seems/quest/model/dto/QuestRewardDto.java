package com.test.seems.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRewardDto {
    
    private Long rewardId;
    private String questName;
    private Integer requiredPoints;
    private String rewardType; // rewardRarity를 rewardType으로 매핑
    private String titleReward;
    private String description;
    private String imagePath;
} 