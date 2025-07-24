package com.test.seems.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String userName;
    private String email;
    private String phone;
    private String profileImage;
    private String joinDate;
    private int status;
    
    // 비밀번호 변경용 필드들
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    // 기본 생성자 (비밀번호 필드 제외)
    public UserInfoResponse(String userName, String email, String phone, String profileImage, String joinDate, int status) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
        this.joinDate = joinDate;
        this.status = status;
    }

    // getter/setter 생략 (Lombok 사용)
}
