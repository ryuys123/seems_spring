package com.test.seems.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDto {
    
    private Long questId;
    private String userId;
    private String questName;
    private Integer questPoints;
    private Integer isCompleted;
    private LocalDateTime createdAt;
} 