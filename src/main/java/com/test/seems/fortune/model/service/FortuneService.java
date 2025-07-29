package com.test.seems.fortune.model.service;

import com.test.seems.fortune.model.dto.DailyMessageResponseDto;
import com.test.seems.fortune.model.dto.MessageHistoryDto;
import com.test.seems.fortune.model.dto.KeywordsDto;
import com.test.seems.fortune.model.dto.KeywordSelectionDto;
import com.test.seems.fortune.model.dto.UserKeywordsStatusDto;

import java.util.List;

public interface FortuneService {
    
    /**
     * 오늘의 행운 메시지 조회 (없으면 자동 생성)
     */
    DailyMessageResponseDto getTodayMessage(String userId);
    
    /**
     * 메시지 히스토리 조회
     */
    List<MessageHistoryDto> getMessageHistory(String userId);
    
    /**
     * 사용 가능한 키워드 목록 조회
     */
    KeywordsDto getAvailableKeywords();
    
    /**
     * 사용자의 키워드 선택 상태 조회
     */
    UserKeywordsStatusDto getUserKeywordsStatus(String userId);
    
    /**
     * 키워드 선택/해제
     */
    UserKeywordsStatusDto updateUserKeywords(KeywordSelectionDto selectionDto);
    
    /**
     * 새로운 행운 메시지 강제 생성 (다시받기 버튼용)
     */
    DailyMessageResponseDto generateNewDailyMessage(String userId);
} 