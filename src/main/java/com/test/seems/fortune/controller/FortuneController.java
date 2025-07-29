package com.test.seems.fortune.controller;

import com.test.seems.fortune.model.dto.DailyMessageResponseDto;
import com.test.seems.fortune.model.dto.MessageHistoryDto;
import com.test.seems.fortune.model.dto.KeywordsDto;
import com.test.seems.fortune.model.dto.KeywordSelectionDto;
import com.test.seems.fortune.model.dto.UserKeywordsStatusDto;
import com.test.seems.fortune.model.service.FortuneService;
import com.test.seems.fortune.exception.FortuneException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/fortune")
@RequiredArgsConstructor
public class FortuneController {

    private final FortuneService fortuneService;

    /**
     * 오늘의 행운 메시지 조회 (자동 생성 포함)
     */
    @GetMapping("/today-message/{userId}")
    public ResponseEntity<DailyMessageResponseDto> getTodayMessage(@PathVariable String userId) {
        try {
            log.info("오늘의 행운 메시지 조회 요청: userId={}", userId);
            
            DailyMessageResponseDto response = fortuneService.getTodayMessage(userId);
            
            log.info("오늘의 행운 메시지 조회 완료: userId={}", userId);
            return ResponseEntity.ok(response);
        } catch (FortuneException e) {
            log.error("오늘의 행운 메시지 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(DailyMessageResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("오늘의 행운 메시지 조회 중 예외 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(DailyMessageResponseDto.builder()
                    .success(false)
                    .message("메시지 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 새로운 행운 메시지 생성 (다시받기 버튼용)
     */
    @PostMapping("/daily-message")
    public ResponseEntity<DailyMessageResponseDto> generateDailyMessage(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            log.info("새로운 행운 메시지 생성 요청: userId={}", userId);
            
            DailyMessageResponseDto response = fortuneService.generateNewDailyMessage(userId);
            
            log.info("새로운 행운 메시지 생성 완료: userId={}", userId);
            return ResponseEntity.ok(response);
        } catch (FortuneException e) {
            log.error("새로운 행운 메시지 생성 실패: userId={}, error={}", request.get("userId"), e.getMessage());
            return ResponseEntity.badRequest().body(DailyMessageResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("새로운 행운 메시지 생성 중 예외 발생: userId={}", request.get("userId"), e);
            return ResponseEntity.internalServerError().body(DailyMessageResponseDto.builder()
                    .success(false)
                    .message("메시지 생성 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 메시지 히스토리 조회
     */
    @GetMapping("/message-history")
    public ResponseEntity<List<MessageHistoryDto>> getMessageHistory(@RequestParam String userId) {
        try {
            log.info("메시지 히스토리 조회 요청: userId={}", userId);
            
            List<MessageHistoryDto> history = fortuneService.getMessageHistory(userId);
            
            log.info("메시지 히스토리 조회 완료: userId={}, count={}", userId, history.size());
            return ResponseEntity.ok(history);
        } catch (FortuneException e) {
            log.error("메시지 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("메시지 히스토리 조회 중 예외 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용 가능한 키워드 목록 조회
     */
    @GetMapping("/keywords")
    public ResponseEntity<KeywordsDto> getAvailableKeywords() {
        try {
            log.info("사용 가능한 키워드 목록 조회 요청");
            
            KeywordsDto keywords = fortuneService.getAvailableKeywords();
            
            log.info("사용 가능한 키워드 목록 조회 완료: count={}", keywords.getKeywords().size());
            return ResponseEntity.ok(keywords);
        } catch (Exception e) {
            log.error("키워드 목록 조회 중 예외 발생", e);
            return ResponseEntity.internalServerError().body(KeywordsDto.builder()
                    .success(false)
                    .message("키워드 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 사용자의 키워드 선택 상태 조회
     */
    @GetMapping("/user-keywords/{userId}")
    public ResponseEntity<UserKeywordsStatusDto> getUserKeywordsStatus(@PathVariable String userId) {
        try {
            log.info("사용자 키워드 상태 조회 요청: userId={}", userId);
            
            UserKeywordsStatusDto status = fortuneService.getUserKeywordsStatus(userId);
            
            log.info("사용자 키워드 상태 조회 완료: userId={}, selectedCount={}", userId, status.getSelectedCount());
            return ResponseEntity.ok(status);
        } catch (FortuneException e) {
            log.error("사용자 키워드 상태 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(UserKeywordsStatusDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("사용자 키워드 상태 조회 중 예외 발생: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(UserKeywordsStatusDto.builder()
                    .success(false)
                    .message("키워드 상태 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 키워드 선택/해제 (마이페이지에서 사용)
     */
    @PostMapping("/update-keywords")
    public ResponseEntity<UserKeywordsStatusDto> updateUserKeywords(@RequestBody KeywordSelectionDto selectionDto) {
        try {
            log.info("키워드 선택/해제 요청: userId={}, selectedCount={}, selectedKeywords={}", 
                    selectionDto.getUserId(), 
                    selectionDto.getSelectedKeywords() != null ? selectionDto.getSelectedKeywords().size() : 0,
                    selectionDto.getSelectedKeywords());
            
            // 요청 데이터 전체 로깅
            log.info("전체 요청 데이터: {}", selectionDto);
            
            UserKeywordsStatusDto status = fortuneService.updateUserKeywords(selectionDto);
            
            log.info("키워드 선택/해제 완료: userId={}, selectedCount={}", selectionDto.getUserId(), status.getSelectedCount());
            return ResponseEntity.ok(status);
        } catch (FortuneException e) {
            log.error("키워드 선택/해제 실패: userId={}, error={}", selectionDto.getUserId(), e.getMessage());
            return ResponseEntity.badRequest().body(UserKeywordsStatusDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("키워드 선택/해제 중 예외 발생: userId={}", selectionDto.getUserId(), e);
            return ResponseEntity.internalServerError().body(UserKeywordsStatusDto.builder()
                    .success(false)
                    .message("키워드 선택/해제 중 오류가 발생했습니다.")
                    .build());
        }
    }
} 