
package com.test.seems.counseling.jpa.entity;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "TB_COUNSELING_SESSIONS")
public class CounselingSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "counseling_session_seq")
    @SequenceGenerator(name = "counseling_session_seq", sequenceName = "SEQ_COUNSELING_SESSIONS_SESSION_ID", allocationSize = 1)
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false) // TB_USERS의 USER_ID 컬럼과 매핑
    private UserEntity user;

    @Column(name = "TOPIC", nullable = false, length = 100)
    private String topic;

    @Column(name = "METHOD", nullable = false, length = 10)
    private String method; // TEXT, VOICE

    @Column(name = "START_TIME", nullable = false)
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    @Builder
    public CounselingSessionEntity(UserEntity user, String topic, String method, Date startTime, Date endTime) {
        this.user = user;
        this.topic = topic;
        this.method = method;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
