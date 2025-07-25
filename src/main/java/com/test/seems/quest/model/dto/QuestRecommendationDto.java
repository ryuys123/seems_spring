package com.test.seems.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRecommendationDto {
    private Long recommendId;
    private String title;
    private String description;
    private Integer duration;
    private Integer reward;
} 