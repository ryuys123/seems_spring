package com.test.seems.user.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.user.model.dto.User;

import java.util.GregorianCalendar;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TB_USERS")
@Entity
public class UserEntity {

    @Id
    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "PHONE", nullable = false, length = 100)
    private String phone;

    @Column(name = "USER_PWD", length = 255)
    private String userPwd;

    @Column(name = "USERNAME", nullable = false, length = 50)
    private String userName;

    @Column(name = "PROFILE_IMAGE")
    private String profileImage;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "CREATED_AT", nullable = false)
    private java.util.Date createdAt;

    @Column(name = "UPDATED_AT")
    private java.util.Date updatedAt;

    @Column(name = "STATUS", nullable = false)
    private int status;

    @Column(name = "ADMIN_YN", length = 1)
    private String adminYn;

    @Column(name = "FACE_LOGIN_ENABLED", nullable = false)
    private Boolean faceLoginEnabled;

    @PrePersist
    public void prePersist() {
        createdAt = new GregorianCalendar().getGregorianChange();    //현재 날짜 시간 적용
        updatedAt = new GregorianCalendar().getGregorianChange();  //현재 날짜 시간 적용
        if (faceLoginEnabled == null) {
            faceLoginEnabled = false; // 기본값은 비활성화
        }
    }

    //entity -> dto 로 변환
    public User toDto() {
        return User.builder()
                .userId(userId)
                .phone(phone)
                .userPwd(userPwd)
                .userName(userName)
                .profileImage(profileImage)
                .email(email)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .status(status)
                .adminYn(adminYn)
                .faceLoginEnabled(faceLoginEnabled)
                .build();
    }
}
