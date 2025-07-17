package com.test.seems.face.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER_FACE_LOGIN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceLoginEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "face_login_seq")
    @SequenceGenerator(name = "face_login_seq", sequenceName = "SEQ_USER_FACE_LOGIN_FACE_LOGIN_ID", allocationSize = 1)
    @Column(name = "FACE_LOGIN_ID")
    private Long faceLoginId;
    
    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "FACE_ID_HASH", nullable = false, length = 255)
    private String faceIdHash;
    
    @Column(name = "FACE_IMAGE_PATH", nullable = false, length = 500)
    private String faceImagePath;
    
    @Column(name = "FACE_NAME", nullable = false, length = 100)
    private String faceName;
    
    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "LAST_USED_AT")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;
    
    @Column(name = "CREATED_BY", length = 255)
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}