
package com.test.seems.counseling.jpa.entity;

import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "TB_COUNSELING_MESSAGES")
public class CounselingMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "counseling_message_seq")
    @SequenceGenerator(name = "counseling_message_seq", sequenceName = "SEQ_COUNSELING_MESSAGES_MESSAGE_ID", allocationSize = 1)
    @Column(name = "MESSAGE_ID")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SESSION_ID", nullable = false) // TB_COUNSELING_SESSIONS의 SESSION_ID 컬럼과 매핑
    private CounselingSessionEntity session;

    @Column(name = "SENDER", nullable = false, length = 50)
    private String sender; // USER, BOT

    @Column(name = "MESSAGE_TYPE", nullable = false, length = 10)
    private String messageType; // TEXT, VOICE, IMAGE

    @Column(name = "MESSAGE_CONTENT", length = 2000)
    private String messageContent;

    @Column(name = "IMAGE_FILE_PATH", length = 255)
    private String imageFilePath;

    @Column(name = "MESSAGE_TIME", nullable = false)
    private Date messageTime;

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
    public CounselingMessageEntity(CounselingSessionEntity session, String sender, String messageType, String messageContent, String imageFilePath, Date messageTime) {
        this.session = session;
        this.sender = sender;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.imageFilePath = imageFilePath;
        this.messageTime = messageTime;
    }
}
