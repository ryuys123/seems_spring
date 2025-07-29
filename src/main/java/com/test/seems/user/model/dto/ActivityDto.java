package com.test.seems.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    
    private String activityType;      // 활동 유형 (COUNSELING, PERSONALITY_TEST, QUEST, CONTENT, EMOTION_LOG, FAQ)
    private String title;             // 활동 제목
    private String description;       // 활동 설명
    private LocalDateTime activityDate; // 활동 일시
    private String status;            // 상태 (완료, 진행중 등)
    private String icon;              // 아이콘 클래스명
    private String color;             // 색상 클래스명
} 