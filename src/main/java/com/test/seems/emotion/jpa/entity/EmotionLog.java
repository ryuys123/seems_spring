package com.test.seems.emotion.jpa.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TB_EMOTION_LOGS")
@Data
public class EmotionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emotion_log_seq")
    @SequenceGenerator(name = "emotion_log_seq", sequenceName = "SEQ_EMOTION_LOGS_EMOTION_LOG_ID", allocationSize = 1)
    @Column(name = "EMOTION_LOG_ID")
    private Long emotionLogId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "EMOTION_ID", nullable = false)
    private Emotion emotion;

    @Column(name = "TEXT_CONTENT", length = 2000)
    private String textContent;

    @Column(name = "CREATED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}