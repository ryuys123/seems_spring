package com.test.seems.content.jpa.entity;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TB_EMOTION_CONTENT_RECOMM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(EmotionContentRecommEntity.PK.class)
public class EmotionContentRecommEntity {
    @Id
    @Column(name = "EMOTION_ID")
    private Long emotionId;

    @Id
    @Column(name = "CONTENT_ID")
    private Long contentId;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long emotionId;
        private Long contentId;
    }
} 