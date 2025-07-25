package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_EMOTION_QUEST_RECOMM")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EmotionQuestRecommEntity.PK.class)
public class EmotionQuestRecommEntity {
    @Id
    @Column(name = "EMOTION_ID")
    private Long emotionId;

    @Id
    @Column(name = "RECOMMEND_ID")
    private Long recommendId;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Long emotionId;
        private Long recommendId;
    }
} 