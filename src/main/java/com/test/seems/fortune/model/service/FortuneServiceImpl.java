package com.test.seems.fortune.model.service;

import com.test.seems.fortune.jpa.entity.DailyMessageEntity;
import com.test.seems.guidance.jpa.entity.GuidanceTypeEntity;
import com.test.seems.fortune.jpa.entity.UserKeywordsEntity;
import com.test.seems.fortune.jpa.repository.DailyMessageRepository;
import com.test.seems.guidance.jpa.repository.GuidanceTypeRepository;
import com.test.seems.fortune.jpa.repository.UserKeywordsRepository;
import com.test.seems.fortune.model.dto.*;
import com.test.seems.fortune.exception.FortuneException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneServiceImpl implements FortuneService {

    private final UserKeywordsRepository userKeywordsRepository;
    private final DailyMessageRepository dailyMessageRepository;
    private final GuidanceTypeRepository guidanceTypeRepository;
    private final RestTemplate restTemplate;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    // 기본 메시지 목록 (Python 서비스 실패 시 사용)
    private static final List<String> DEFAULT_MESSAGES = Arrays.asList(
            "오늘은 특별한 하루가 될 것입니다. 긍정적인 마음으로 시작해보세요.",
            "작은 변화가 큰 차이를 만듭니다. 한 걸음씩 나아가세요.",
            "당신의 노력이 곧 빛을 발할 것입니다. 포기하지 마세요.",
            "주변의 따뜻한 마음들이 당신을 지지하고 있습니다.",
            "오늘 하루도 힘내세요. 당신은 충분히 잘하고 있습니다.",
            "새로운 시작의 기회입니다. 과거에 얽매이지 마세요.",
            "당신의 내면의 힘이 당신을 이끌어줄 것입니다.",
            "오늘 만난 모든 사람에게 따뜻한 미소를 나누어보세요.",
            "작은 성취도 축하할 가치가 있습니다. 자신을 칭찬해주세요.",
            "변화는 언제나 가능합니다. 오늘부터 새로운 마음으로 시작해보세요."
    );

    @Override
    @Transactional
    public DailyMessageResponseDto getTodayMessage(String userId) {
        try {
            log.info("오늘의 행운 메시지 조회 시작: userId={}", userId);
            
            LocalDate today = LocalDate.now();
            
            // 1. 오늘 메시지가 이미 있는지 확인 (중복 데이터 처리)
            List<DailyMessageEntity> existingMessages = dailyMessageRepository.findByUserIdAndMessageDate(userId, today);
            
            if (!existingMessages.isEmpty()) {
                // 가장 최근 메시지를 반환 (ORDER BY createdDate DESC로 정렬됨)
                DailyMessageEntity message = existingMessages.get(0);
                String keywordName = getKeywordNameByGuidanceTypeId(message.getGuidanceTypeId());
                log.info("기존 오늘 메시지 반환: userId={}, keyword={}, messageId={}", userId, keywordName, message.getMessageId());
                
                // 중복 데이터가 있으면 정리
                if (existingMessages.size() > 1) {
                    log.warn("중복 메시지 발견: userId={}, count={}, 최신 메시지 ID={}", userId, existingMessages.size(), message.getMessageId());
                    // 최신 메시지 외의 중복 데이터 삭제
                    for (int i = 1; i < existingMessages.size(); i++) {
                        dailyMessageRepository.delete(existingMessages.get(i));
                        log.info("중복 메시지 삭제: messageId={}", existingMessages.get(i).getMessageId());
                    }
                }
                
                return DailyMessageResponseDto.builder()
                        .success(true)
                        .message("오늘의 행운 메시지를 성공적으로 조회했습니다.")
                        .userId(userId)
                        .dailyMessage(message.getMessageContent())
                        .selectedKeyword(keywordName)
                        .messageDate(today.toString())
                        .build();
            }
            
            // 2. 오늘 메시지가 없으면 새로 생성
            log.info("오늘 메시지 없음 - 새로 생성: userId={}", userId);
            
            // 사용자의 선택된 키워드 조회
            List<UserKeywordsEntity> userKeywords = userKeywordsRepository.findAllKeywordsByUserId(userId);
            List<Long> selectedGuidanceTypeIds = userKeywords.stream()
                    .filter(entity -> entity.getIsSelected() != null && entity.getIsSelected())
                    .map(UserKeywordsEntity::getGuidanceTypeId)
                    .collect(Collectors.toList());
            
            if (selectedGuidanceTypeIds.isEmpty()) {
                throw new FortuneException("선택된 키워드가 없습니다. 마이페이지에서 키워드를 선택해주세요.");
            }
            
            // 선택된 키워드 중 하나를 무작위로 선택
            Long selectedGuidanceTypeId = getRandomGuidanceTypeIdFromList(selectedGuidanceTypeIds);
            String selectedKeyword = getKeywordNameByGuidanceTypeId(selectedGuidanceTypeId);
            log.info("선택된 키워드: userId={}, keyword={}", userId, selectedKeyword);
            
            // Python AI 서비스 호출하여 메시지 생성
            String messageContent = callPythonAIService(selectedKeyword);
            if (messageContent == null) {
                messageContent = getDefaultMessage();
                log.warn("Python AI 서비스 실패 - 기본 메시지 사용: userId={}, keyword={}", userId, selectedKeyword);
            } else {
                log.info("AI 서비스로부터 메시지 생성 성공: userId={}, keyword={}", userId, selectedKeyword);
            }
            
            // 3. 새 메시지 저장
            DailyMessageEntity newMessage = DailyMessageEntity.builder()
                    .userId(userId)
                    .messageDate(today)
                    .guidanceTypeId(selectedGuidanceTypeId)
                    .messageContent(messageContent)
                    .createdDate(LocalDateTime.now())
                    .build();
            
            dailyMessageRepository.save(newMessage);
            log.info("새 메시지 저장 완료: userId={}, keyword={}", userId, selectedKeyword);
            
            return DailyMessageResponseDto.builder()
                    .success(true)
                    .message("오늘의 행운 메시지를 성공적으로 생성했습니다.")
                    .userId(userId)
                    .dailyMessage(messageContent)
                    .selectedKeyword(selectedKeyword)
                    .messageDate(today.toString())
                    .build();
                    
        } catch (FortuneException e) {
            throw e;
        } catch (Exception e) {
            log.error("오늘의 행운 메시지 조회 중 오류 발생: userId={}", userId, e);
            throw new FortuneException("메시지 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageHistoryDto> getMessageHistory(String userId) {
        try {
            List<DailyMessageEntity> messages = dailyMessageRepository.findMessageHistoryByUserId(userId);
            
            return messages.stream()
                    .map(message -> MessageHistoryDto.builder()
                            .messageId(message.getMessageId())
                            .userId(message.getUserId())
                            .messageContent(message.getMessageContent())
                            .selectedKeyword(getKeywordNameByGuidanceTypeId(message.getGuidanceTypeId()))
                            .messageDate(message.getMessageDate().toString())
                            .createdDate(message.getCreatedDate())
                            .build())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("메시지 히스토리 조회 중 오류 발생: userId={}", userId, e);
            throw new FortuneException("메시지 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public KeywordsDto getAvailableKeywords() {
        try {
            List<GuidanceTypeEntity> guidanceTypes = guidanceTypeRepository.findAll();
            List<String> keywords = guidanceTypes.stream()
                    .map(GuidanceTypeEntity::getGuidanceTypeName)
                    .collect(Collectors.toList());
            
            return KeywordsDto.builder()
                    .success(true)
                    .message("사용 가능한 키워드 목록을 성공적으로 조회했습니다.")
                    .keywords(keywords)
                    .totalCount(keywords.size())
                    .build();
        } catch (Exception e) {
            log.error("키워드 목록 조회 중 오류 발생", e);
            throw new FortuneException("키워드 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserKeywordsStatusDto getUserKeywordsStatus(String userId) {
        try {
            log.info("사용자 키워드 상태 조회 시작: userId={}", userId);
            
            // 사용자의 모든 키워드 상태 조회
            List<UserKeywordsEntity> userKeywords = userKeywordsRepository.findAllKeywordsByUserId(userId);
            log.info("DB에서 조회된 키워드 개수: userId={}, count={}", userId, userKeywords.size());
            
            // 모든 가이드 유형 조회
            List<GuidanceTypeEntity> allGuidanceTypes = guidanceTypeRepository.findAll();
            
            // 선택된 키워드 목록
            List<String> selectedKeywords = userKeywords.stream()
                    .filter(entity -> entity.getIsSelected() != null && entity.getIsSelected())
                    .map(entity -> getKeywordNameByGuidanceTypeId(entity.getGuidanceTypeId()))
                    .filter(keyword -> keyword != null)
                    .collect(Collectors.toList());
            
            // 모든 키워드의 선택 상태 맵 생성
            Map<String, Boolean> allKeywordsStatus = new HashMap<>();
            for (GuidanceTypeEntity guidanceType : allGuidanceTypes) {
                boolean isSelected = userKeywords.stream()
                        .anyMatch(uk -> uk.getGuidanceTypeId().equals(guidanceType.getGuidanceTypeId()) && 
                                uk.getIsSelected() != null && uk.getIsSelected());
                allKeywordsStatus.put(guidanceType.getGuidanceTypeName(), isSelected);
            }
            
            // 포춘쿠키 활성화 여부 (최소 1개 이상 선택)
            boolean isFortuneCookieEnabled = selectedKeywords.size() >= 1;
            
            log.info("키워드 상태 조회 완료: userId={}, selectedCount={}, totalCount={}, enabled={}", 
                    userId, selectedKeywords.size(), allGuidanceTypes.size(), isFortuneCookieEnabled);
            
            return UserKeywordsStatusDto.builder()
                    .success(true)
                    .message("사용자 키워드 상태를 성공적으로 조회했습니다.")
                    .userId(userId)
                    .selectedKeywords(selectedKeywords)
                    .allKeywordsStatus(allKeywordsStatus)
                    .isFortuneCookieEnabled(isFortuneCookieEnabled)
                    .selectedCount(selectedKeywords.size())
                    .totalCount(allGuidanceTypes.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("사용자 키워드 상태 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new FortuneException("키워드 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserKeywordsStatusDto updateUserKeywords(KeywordSelectionDto selectionDto) {
        try {
            String userId = selectionDto.getUserId();
            List<String> selectedKeywords = selectionDto.getSelectedKeywords();
            
            // null이나 빈 키워드 필터링
            List<String> validKeywords = selectedKeywords.stream()
                    .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                    .collect(Collectors.toList());
            
            log.info("원본 키워드: {}, 필터링된 키워드: {}", selectedKeywords, validKeywords);
            log.info("selectedKeywords 타입: {}, 크기: {}", selectedKeywords.getClass().getSimpleName(), selectedKeywords.size());
            if (!selectedKeywords.isEmpty()) {
                log.info("첫 번째 키워드: '{}', 타입: {}", selectedKeywords.get(0), selectedKeywords.get(0) != null ? selectedKeywords.get(0).getClass().getSimpleName() : "null");
            }
            
            // 최소 1개 이상 선택해야 함
            if (validKeywords.isEmpty()) {
                throw new FortuneException("최소 1개 이상의 유효한 키워드를 선택해야 합니다. 선택된 키워드: " + selectedKeywords);
            }
            
            // 모든 키워드 상태를 false로 초기화
            List<UserKeywordsEntity> existingKeywords = userKeywordsRepository.findAllKeywordsByUserId(userId);
            for (UserKeywordsEntity keyword : existingKeywords) {
                keyword.setIsSelected(false);
            }
            userKeywordsRepository.saveAll(existingKeywords);
            
            // 선택된 키워드들을 true로 설정
            for (String keywordName : validKeywords) {
                // null이나 빈 문자열 체크
                if (keywordName == null || keywordName.trim().isEmpty()) {
                    log.warn("null 또는 빈 키워드 무시: {}", keywordName);
                    continue;
                }
                
                // 키워드 이름으로 GUIDANCE_TYPE_ID 찾기
                Long guidanceTypeId = getGuidanceTypeIdByKeywordName(keywordName);
                if (guidanceTypeId == null) {
                    throw new FortuneException("유효하지 않은 키워드입니다: " + keywordName);
                }
                
                // 기존 키워드가 있으면 업데이트, 없으면 새로 생성
                Optional<UserKeywordsEntity> existing = userKeywordsRepository.findByUserIdAndGuidanceTypeId(userId, guidanceTypeId);
                if (existing.isPresent()) {
                    UserKeywordsEntity entity = existing.get();
                    entity.setIsSelected(true);
                    userKeywordsRepository.save(entity);
                } else {
                    UserKeywordsEntity newEntity = UserKeywordsEntity.builder()
                            .userId(userId)
                            .guidanceTypeId(guidanceTypeId)
                            .isSelected(true)
                            .updatedDate(LocalDateTime.now())
                            .build();
                    userKeywordsRepository.save(newEntity);
                }
            }
            
            // 업데이트된 상태 반환
            return getUserKeywordsStatus(userId);
            
        } catch (FortuneException e) {
            throw e;
        } catch (Exception e) {
            log.error("키워드 선택/해제 중 오류 발생: userId={}", selectionDto.getUserId(), e);
            throw new FortuneException("키워드 선택/해제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 새로운 행운 메시지 강제 생성 (다시받기 버튼용)
     */
    public DailyMessageResponseDto generateNewDailyMessage(String userId) {
        try {
            log.info("새로운 행운 메시지 강제 생성 시작: userId={}", userId);
            
            // 1. 오늘 날짜로 기존 메시지 삭제 (중복 데이터 처리)
            LocalDate today = LocalDate.now();
            List<DailyMessageEntity> existingMessages = dailyMessageRepository.findByUserIdAndMessageDate(userId, today);
            if (!existingMessages.isEmpty()) {
                // 모든 중복 메시지 삭제
                for (DailyMessageEntity message : existingMessages) {
                    dailyMessageRepository.delete(message);
                    log.info("기존 메시지 삭제 완료: userId={}, messageId={}", userId, message.getMessageId());
                }
                log.info("총 {}개의 기존 메시지 삭제 완료: userId={}", existingMessages.size(), userId);
            }
            
            // 2. 새로운 메시지 생성 (기존 getTodayMessage 로직과 동일)
            return getTodayMessage(userId);
            
        } catch (Exception e) {
            log.error("새로운 행운 메시지 생성 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new FortuneException("새로운 메시지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Python AI 서비스 호출
     */
    private String callPythonAIService(String keyword) {
        try {
            String url = pythonApiUrl + "/api/emotion/daily-message";
            
            Map<String, Object> requestBody = Map.of(
                "keyword", keyword,
                "model", "gpt-3.5-turbo"
            );

            log.info("Python AI 서비스 호출: url={}, keyword={}", url, keyword);
            
            // RestTemplate을 사용하여 Python 서비스 호출
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response != null && response.containsKey("message")) {
                String aiMessage = (String) response.get("message");
                log.info("AI 서비스 응답 성공: keyword={}, message={}", keyword, aiMessage);
                return aiMessage;
            }
            
            log.warn("AI 서비스 응답에 message 필드가 없음: response={}", response);
            return null;
        } catch (Exception e) {
            log.error("Python AI 서비스 호출 중 오류 발생: keyword={}, error={}", keyword, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 선택된 GUIDANCE_TYPE_ID 목록에서 랜덤 선택
     */
    private Long getRandomGuidanceTypeIdFromList(List<Long> guidanceTypeIds) {
        Random random = new Random();
        return guidanceTypeIds.get(random.nextInt(guidanceTypeIds.size()));
    }

    /**
     * 기본 메시지 선택
     */
    private String getDefaultMessage() {
        Random random = new Random();
        return DEFAULT_MESSAGES.get(random.nextInt(DEFAULT_MESSAGES.size()));
    }

    /**
     * GUIDANCE_TYPE_ID로 키워드 이름 찾기
     */
    private String getKeywordNameByGuidanceTypeId(Long guidanceTypeId) {
        if (guidanceTypeId == null) {
            return null;
        }
        Optional<GuidanceTypeEntity> guidanceType = guidanceTypeRepository.findById(guidanceTypeId);
        return guidanceType.map(GuidanceTypeEntity::getGuidanceTypeName).orElse(null);
    }

    /**
     * 키워드 이름으로 GUIDANCE_TYPE_ID 찾기
     */
    private Long getGuidanceTypeIdByKeywordName(String keywordName) {
        if (keywordName == null) {
            return null;
        }
        Optional<GuidanceTypeEntity> guidanceType = guidanceTypeRepository.findAll()
                .stream()
                .filter(gt -> gt.getGuidanceTypeName().equals(keywordName))
                .findFirst();
        return guidanceType.map(GuidanceTypeEntity::getGuidanceTypeId).orElse(null);
    }
} 