package com.test.seems.fortune.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DAILY_MESSAGES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long messageId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "MESSAGE_DATE", nullable = false)
    private LocalDate messageDate;

    @Column(name = "GUIDANCE_TYPE_ID", nullable = false)
    private Long guidanceTypeId;

    @Column(name = "MESSAGE_CONTENT", nullable = false, length = 1000)
    private String messageContent;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (messageDate == null) {
            messageDate = LocalDate.now();
        }
    }
} 