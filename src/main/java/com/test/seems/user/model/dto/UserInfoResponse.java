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

    // getter/setter 생략 (Lombok 사용)
}
