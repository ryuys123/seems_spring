package com.test.seems.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    
    private Integer level;
    private Integer completedQuests;
    private Integer totalQuests;
    private Integer ownedTitles;
    private Integer totalTitles;
} 