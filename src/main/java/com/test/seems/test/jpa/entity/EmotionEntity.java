package com.test.seems.test.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_EMOTIONS")
@Data
@NoArgsConstructor
public class EmotionEntity {

    @Id
    @Column(name = "EMOTION_ID")
    private Long emotionId;

    @Column(name = "EMOTION_NAME", nullable = false, length = 50)
    private String emotionName;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}
