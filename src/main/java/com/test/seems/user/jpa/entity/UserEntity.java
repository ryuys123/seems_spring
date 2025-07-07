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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "PHONE", nullable = false, length = 100)
    private String phone;
    
    @Column(name = "PASSWORD_HASH", length = 255)
    private String passwordHash;
    
    @Column(name = "USERNAME", nullable = false, length = 50)
    private String userName;
    
    @Column(name = "PROFILE_IMAGE")
    private String profileImage;
    
    @Column(name = "CREATED_AT", nullable = false)
    private java.util.Date createdAt;
    
    @Column(name = "UPDATED_AT")
    private java.util.Date updatedAt;
    
    @Column(name = "STATUS", nullable = false)
    private int status;
    
    @Column(name = "ADMIN_YN", length = 1)
    private String adminYn;
    
    @PrePersist
    public void prePersist() {
        createdAt = new GregorianCalendar().getGregorianChange();    //현재 날짜 시간 적용
        updatedAt = new GregorianCalendar().getGregorianChange();  //현재 날짜 시간 적용
    }

    //entity -> dto 로 변환
    public User toDto() {
        return User.builder()
                .userId(userId)
                .phone(phone)
                .passwordHash(passwordHash)
                .userName(userName)
                .profileImage(profileImage)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .status(status)
                .adminYn(adminYn)
                .build();
    }
}
