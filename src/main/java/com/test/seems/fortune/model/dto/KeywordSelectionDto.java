package com.test.seems.fortune.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordSelectionDto {
    
    private String userId;
    private List<String> selectedKeywords; // 선택된 키워드 목록
} 