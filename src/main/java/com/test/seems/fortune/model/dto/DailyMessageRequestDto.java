package com.test.seems.fortune.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMessageRequestDto {
    
    private String userId;
    private String selectedKeyword; // 선택된 키워드 (선택사항)
} 