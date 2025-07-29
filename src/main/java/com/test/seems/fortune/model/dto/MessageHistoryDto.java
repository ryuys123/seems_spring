package com.test.seems.fortune.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHistoryDto {
    
    private Long messageId;
    private String userId;
    private String messageContent;
    private String selectedKeyword;
    private String messageDate;
    private LocalDateTime createdDate;
} 