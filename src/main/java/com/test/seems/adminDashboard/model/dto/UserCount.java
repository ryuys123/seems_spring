package com.test.seems.adminDashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data  // @Getter, @Setter, @ToString, @Equals, @HashCode 오버라이딩 까지 자동 코드 생성해 주는 어노테이션임
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCount {
    private Long totalUsers; // 전체 사용자 (탈퇴자 제외)
    private Long totalWithdraws; // 전체 탈퇴자
//    private Long totalEmotionLogs;
//    private Long totalCounselings;
//    private Long recentJoinCount;
    private List<Map<String, Object>> dailyJoinStats; // 일별 가입자 수
    private List<Map<String, Object>> weeklyJoinStats;  // 주별 가입자 수
    private List<Map<String, Object>> monthlyJoinStats; // 월별 가입자 수

}
