package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_QUEST_RECOMMENDATIONS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRecommendationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_QUEST_RECOMMENDATIONS_RECOMMENDATION_ID")
    @SequenceGenerator(name = "SEQ_QUEST_RECOMMENDATIONS_RECOMMENDATION_ID", sequenceName = "SEQ_QUEST_RECOMMENDATIONS_RECOMMENDATION_ID", allocationSize = 1)
    @Column(name = "RECOMMEND_ID")
    private Long recommendId;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "DURATION")
    private Integer duration;

    @Column(name = "REWARD")
    private Integer reward;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
} 