package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER_REWARDS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRewardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userRewardSeq")
    @SequenceGenerator(name = "userRewardSeq", sequenceName = "SEQ_USER_REWARDS_USER_REWARD_ID", allocationSize = 1)
    @Column(name = "USER_REWARD_ID")
    private Long userRewardId;
    
    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "REWARD_ID", nullable = false)
    private Long rewardId;
    
    @Column(name = "ACQUIRED_AT")
    private LocalDateTime acquiredAt;
    
    @Column(name = "IS_APPLIED")
    private Integer isApplied;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REWARD_ID", insertable = false, updatable = false)
    private QuestRewardEntity questReward;
} 