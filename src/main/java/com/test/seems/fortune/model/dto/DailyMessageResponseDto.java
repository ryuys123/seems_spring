package com.test.seems.fortune.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMessageResponseDto {
    private boolean success;
    private String message;
    private String userId;
    private String dailyMessage;
    private String selectedKeyword;
    private String messageDate;
} 