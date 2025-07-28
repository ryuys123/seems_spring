package com.test.seems.adminDashboard.controller;

import com.test.seems.adminDashboard.model.dto.CounselingStats;
import com.test.seems.adminDashboard.model.dto.EmotionStats;
import com.test.seems.adminDashboard.model.dto.VisitorStats;
import com.test.seems.adminDashboard.model.dto.UserCount;
import com.test.seems.adminDashboard.model.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j   // log 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class AdminDashboardController {

    private final AdminDashboardService admindashboardService;

    // 사용자 통계
    @GetMapping("/admindashboard/usercount")
    public ResponseEntity<UserCount> getDashboardData() {
        return ResponseEntity.ok(admindashboardService.getDashboardData());
    }

    // 방문자 통계
    @GetMapping("/admindashboard/visitorcount")
    public ResponseEntity<VisitorStats> getVisitorStats() {
        return ResponseEntity.ok(admindashboardService.getVisitorStats());
    }

    //    // 감정기록 통계
    @GetMapping("/admindashboard/emotioncount")
    public ResponseEntity<EmotionStats> getEmotionStats() {
        return ResponseEntity.ok(admindashboardService.getEmotionStats());
    }

    //    // 상담 통계
    @GetMapping("/admindashboard/counselingcount")
    public ResponseEntity<CounselingStats> getCounselingStats() {
        return ResponseEntity.ok(admindashboardService.getCounselingStats());
    }
}