package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_QUEST")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_QUEST_QUEST_ID")
    @SequenceGenerator(name = "SEQ_QUEST_QUEST_ID", sequenceName = "SEQ_QUEST_QUEST_ID", allocationSize = 1)
    @Column(name = "QUEST_ID")
    private Long questId;
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
    
    @Column(name = "QUEST_NAME", nullable = false, length = 200)
    private String questName;
    
    @Column(name = "QUEST_POINTS", nullable = false)
    private Integer questPoints;
    
    @Column(name = "IS_COMPLETED", nullable = false)
    private Integer isCompleted;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
} 