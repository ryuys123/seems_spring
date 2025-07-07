package com.test.seems.user.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.test.seems.user.jpa.entity.UserEntity;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    //Field == Property (멤버변수 == 속성)
    //validation 검사는 뷰에서 컨트롤러 command 객체로 값 전송될 때 검사자 작동됨
    @NotBlank
    private String userId;           // USER_ID VARCHAR2(255) PRIMARY KEY
    @NotBlank
    private String phone;          // PHONE VARCHAR2(100) NOT NULL
    private String passwordHash;   // PASSWORD_HASH VARCHAR2(255)
    @NotBlank
    private String userName;       // USERNAME VARCHAR2(50) NOT NULL
    private String profileImage;   // PROFILE_IMAGE BLOB
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.util.Date createdAt;        // CREATED_AT DATE DEFAULT SYSDATE NOT NULL
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.util.Date updatedAt;        // UPDATED_AT DATE
    @NotBlank
    private int status;        // STATUS NUMBER(1) DEFAULT 1 NOT NULL
    private String adminYn;        // ADMIN_YN VARCHAR2(1) DEFAULT 'N'

    public UserEntity toEntity() {
        return UserEntity.builder()
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
