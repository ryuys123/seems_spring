package com.test.seems.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindPasswordResponse {
    private boolean success;
    private String message;
}
