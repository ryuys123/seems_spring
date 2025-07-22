package com.test.seems.counseling.controller;

import com.test.seems.counseling.model.dto.CounselingDto;
import com.test.seems.counseling.model.service.CounselingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
public class CounselingController {

    private final CounselingService counselingService;

    @PostMapping("/save")
    public ResponseEntity<CounselingDto.HistoryResponse> saveCounselingHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CounselingDto.CreateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        CounselingDto.HistoryResponse response = counselingService.saveCounselingHistory(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CounselingDto.HistoryResponse>> getCounselingHistoryList(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        List<CounselingDto.HistoryResponse> historyList = counselingService.getCounselingHistoryList(userDetails.getUsername());
        return ResponseEntity.ok(historyList);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<CounselingDto.DetailResponse> getCounselingHistoryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        CounselingDto.DetailResponse detail = counselingService.getCounselingHistoryDetail(userDetails.getUsername(), sessionId);
        return ResponseEntity.ok(detail);
    }
}