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
public class KeywordsDto {
    
    private boolean success;
    private String message;
    private List<String> keywords;
    private int totalCount;
} 