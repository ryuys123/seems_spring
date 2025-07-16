package com.test.seems.emotion.jpa.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "TB_EMOTIONS")
@Data
public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emotion_seq")
    @SequenceGenerator(name = "emotion_seq", sequenceName = "SEQ_EMOTIONS_EMOTION_ID", allocationSize = 1)
    @Column(name = "EMOTION_ID")
    private Long emotionId;

    @Column(name = "EMOTION_NAME", nullable = false, unique = true)
    private String emotionName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Transient
    private String emoji;
}