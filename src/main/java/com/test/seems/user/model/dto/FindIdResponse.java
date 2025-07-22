package com.test.seems.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindIdResponse {
    private String userId;
    private String username; // 선택
    private String message;
}
