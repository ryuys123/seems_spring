package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_QUEST_REWARDS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRewardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REWARD_ID")
    private Long rewardId;
    
    @Column(name = "QUEST_NAME", nullable = false, length = 100)
    private String questName;
    
    @Column(name = "REQUIRED_POINTS", nullable = false)
    private Integer requiredPoints;
    
    @Column(name = "REWARD_RARITY", nullable = false, length = 20)
    private String rewardRarity;
    
    @Column(name = "TITLE_REWARD", length = 50)
    private String titleReward;
    
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;
    
    @Column(name = "IMAGE_PATH", length = 255)
    private String imagePath;
} 