package com.test.seems.face.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER_FACE_LOGIN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceLoginEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FACE_LOGIN_ID")
    private Long faceLoginId;
    
    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "FACE_IMAGE_PATH", nullable = false, length = 500)
    private String faceImagePath;
    
    @Column(name = "FACE_NAME", nullable = false, length = 100)
    private String faceName;
    
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}