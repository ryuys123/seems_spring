package com.test.seems.fortune.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKeywordsStatusDto {
    
    private boolean success;
    private String message;
    private String userId;
    private List<String> selectedKeywords;
    private Map<String, Boolean> allKeywordsStatus; // 모든 키워드의 선택 상태
    private boolean isFortuneCookieEnabled; // 포춘쿠키 활성화 여부
    private int selectedCount;
    private int totalCount;
} 