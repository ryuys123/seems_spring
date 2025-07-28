package com.test.seems.adminDashboard.model.service;

import com.test.seems.adminDashboard.model.dto.CounselingStats;
import com.test.seems.adminDashboard.model.dto.EmotionStats;
import com.test.seems.adminDashboard.model.dto.VisitorStats;
import com.test.seems.adminDashboard.model.dto.UserCount;
import com.test.seems.counseling.jpa.repository.CounselingSessionRepository;
import com.test.seems.emotion.jpa.repository.EmotionLogRepository;
import com.test.seems.log.jpa.repository.LogRepository;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j   // Logger 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@Service
@RequiredArgsConstructor
@Transactional
public class AdminDashboardService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final LogRepository logRepository;
    @Autowired
    private final EmotionLogRepository emotionLogRepository;
    @Autowired
    private final CounselingSessionRepository counselingSessionRepository;


    // 사용자 통계
    public UserCount getDashboardData() {
        Date sevenDaysAgo = Date.from(
                LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        return UserCount.builder()
                .totalUsers(userRepository.countAllUsers())
                .totalWithdraws(userRepository.countAllWithdraws())
//                .recentJoinCount(userRepository.countRecentJoins(sevenDaysAgo))
                .dailyJoinStats(userRepository.getDailyJoinStats(sevenDaysAgo))
                .weeklyJoinStats(userRepository.getWeeklyJoinStats())   // 주별 가입자 수
                .monthlyJoinStats(userRepository.getMonthlyJoinStats()) // 월별 가입자 수
                .build();
    }

    // 방문자 통계
    public VisitorStats getVisitorStats() {
        List<Map<String, Object>> daily = logRepository.getDailyVisitorStats();
        List<Map<String, Object>> weekly = logRepository.getWeeklyVisitorStats();
        List<Map<String, Object>> monthly = logRepository.getMonthlyVisitorStats();

        return VisitorStats.builder()
                .dailyVisitorStats(daily)
                .weeklyVisitorStats(weekly)
                .monthlyVisitorStats(monthly)
                .build();
    }

   // 감정기록 통계
public EmotionStats getEmotionStats() {
    List<Map<String, Object>> weeklyStats = emotionLogRepository.getWeeklyEmotionStats();

    long total = weeklyStats.stream()
            .mapToLong(item -> ((Number) item.get("count")).longValue())
            .sum();

    return EmotionStats.builder()
            .totalEmotionLogs(total)
            .weeklyEmotionStats(weeklyStats)
            .build();
}

    // 상담기록 통계
public CounselingStats getCounselingStats() {
    List<Map<String, Object>> weeklyStats = counselingSessionRepository.getWeeklyCounselingStats();

    long total = weeklyStats.stream()
            .mapToLong(item -> ((Number) item.get("count")).longValue())
            .sum();

    return CounselingStats.builder()
            .totalCounselingLogs(total)
            .weeklyCounselingStats(weeklyStats)
            .build();
}
}