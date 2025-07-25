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
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "FACE_NAME", length = 255)
    private String faceName;

    @Column(name = "FACE_IMAGE_PATH", length = 255)
    private String faceImagePath;

    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "LAST_USED_AT")
    private LocalDateTime lastUsedAt;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive;

    @Column(name = "CREATED_BY", length = 255)
    private String createdBy;

    // 사용자 정보와의 관계 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false)
    private com.test.seems.user.jpa.entity.UserEntity user;

    // 생성 시 기본값 설정
    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = 1;
        }
    }

    // 활성 상태 확인 메소드
    public boolean isActive() {
        return isActive == 1;
    }

    // 비활성 상태로 변경
    public void deactivate() {
        this.isActive = 0;
    }

    // 활성 상태로 변경
    public void activate() {
        this.isActive = 1;
    }

    // 마지막 사용 시간 업데이트
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
}