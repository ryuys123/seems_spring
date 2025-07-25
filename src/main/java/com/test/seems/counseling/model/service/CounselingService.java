package com.test.seems.counseling.model.service;

import com.test.seems.ai.service.AIService;
import com.test.seems.counseling.model.dto.CounselingDto;
import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import com.test.seems.counseling.jpa.repository.CounselingMessageRepository;
import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import com.test.seems.counseling.jpa.repository.CounselingSessionRepository;
import com.test.seems.counseling.jpa.entity.CounselingAnalysisSummaryEntity; // 추가
import com.test.seems.counseling.jpa.repository.CounselingAnalysisSummaryRepository; // 추가
import com.test.seems.guidance.jpa.entity.GuidanceTypeEntity; // 추가
import com.test.seems.guidance.jpa.repository.GuidanceTypeRepository; // 추가
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map; // 추가
import java.util.stream.Collectors;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselingService {

    private final CounselingSessionRepository counselingSessionRepository;
    private final CounselingMessageRepository counselingMessageRepository;
    private final UserRepository userRepository;
    private final AIService aiService; // 추가
    private final CounselingAnalysisSummaryRepository counselingAnalysisSummaryRepository; // 추가
    private final GuidanceTypeRepository guidanceTypeRepository; // 추가

    @Transactional
    public CounselingDto.HistoryResponse saveCounselingHistory(String username, CounselingDto.CreateRequest request) {
        log.info("Saving counseling history for user: {}", username);
        log.info("Request topic: {}, method: {}, sessionId: {}", request.getTopic(), request.getMethod(), request.getSessionId());
        log.info("Number of messages: {}", request.getMessages().size());

        UserEntity user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 1. 세션 처리 (기존 세션 사용 또는 새 세션 생성)
        CounselingSessionEntity sessionEntity;
        if (request.getSessionId() != null) {
            sessionEntity = counselingSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + request.getSessionId()));
            // 세션의 사용자 일치 여부 확인 (보안 강화)
            if (!sessionEntity.getUser().getUserId().equals(username)) {
                throw new IllegalArgumentException("Access denied: This counseling session does not belong to the user.");
            }
            // 기존 세션에 토픽이나 방식이 변경될 수 있다면 업데이트 로직 추가
            if (request.getTopic() != null && !request.getTopic().isEmpty()) {
                sessionEntity.setTopic(request.getTopic());
            }
            if (request.getMethod() != null && !request.getMethod().isEmpty()) {
                sessionEntity.setMethod(request.getMethod());
            }
            counselingSessionRepository.save(sessionEntity); // 변경사항 저장
        } else {
            sessionEntity = CounselingSessionEntity.builder()
                    .user(user)
                    .topic(request.getTopic())
                    .method(request.getMethod())
                    .startTime(new Date()) // 현재 시간으로 시작 시간 설정
                    .build();
            counselingSessionRepository.save(sessionEntity);
        }

        // 2. 메시지 저장
        List<CounselingMessageEntity> messageEntities = request.getMessages().stream()
                .map(messageDto -> {
                    log.info("Saving message - sender: {}, type: {}, content length: {}", messageDto.getType(), messageDto.getText(), messageDto.getText().length());
                    return CounselingMessageEntity.builder()
                            .session(sessionEntity) // 기존 또는 새로 생성된 세션에 연결
                            .sender(messageDto.getType().toUpperCase()) // USER, AI
                            .messageType("TEXT") // 현재는 텍스트만 지원
                            .messageContent(messageDto.getText())
                            .messageTime(new Date()) // 현재 시간으로 메시지 시간 설정
                            .build();
                })
                .collect(Collectors.toList());
        counselingMessageRepository.saveAll(messageEntities); // 모든 메시지를 한 번에 저장

        // 3. AI를 통한 상담 내용 요약 및 저장
        try {
            Map<String, Object> aiSummaryResult = aiService.summarizeCounseling(messageEntities);

            String summaryContent = (String) aiSummaryResult.get("summaryContent");
            List<String> extractedKeywords = (List<String>) aiSummaryResult.get("extractedKeywords");

            GuidanceTypeEntity guidanceType = null;
            if (extractedKeywords != null && !extractedKeywords.isEmpty()) {
                // 첫 번째 키워드를 사용하여 GuidanceType 조회
                Optional<GuidanceTypeEntity> foundGuidanceType = guidanceTypeRepository.findByGuidanceTypeName(extractedKeywords.get(0));
                if (foundGuidanceType.isPresent()) {
                    guidanceType = foundGuidanceType.get();
                } else {
                    log.warn("No GuidanceType found for keyword: {}. Using default or null.", extractedKeywords.get(0));
                    // TODO: 적절한 기본 GuidanceType을 설정하거나, "기타"와 같은 GuidanceType을 미리 DB에 추가하고 사용하도록 로직 개선 필요
                }
            }

            CounselingAnalysisSummaryEntity summaryEntity = CounselingAnalysisSummaryEntity.builder()
                    .session(sessionEntity)
                    .summaryType("TEXT") // 현재는 텍스트 기반 요약
                    .guidanceType(guidanceType) // 조회된 GuidanceTypeEntity 설정
                    .summaryContent(summaryContent)
                    .build();
            counselingAnalysisSummaryRepository.save(summaryEntity);
            log.info("Counseling analysis summary saved successfully for session: {}", sessionEntity.getSessionId());

        } catch (Exception e) {
            log.error("Failed to summarize counseling or save summary: {}", e.getMessage(), e);
            // AI 요약 실패 시에도 상담 기록 저장은 성공으로 처리 (필요에 따라 롤백 정책 변경 가능)
        }

        return CounselingDto.HistoryResponse.fromEntity(sessionEntity);
    }

    @Transactional(readOnly = true)
    public List<CounselingDto.HistoryResponse> getCounselingHistoryList(String username) {
        UserEntity user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return counselingSessionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(CounselingDto.HistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CounselingDto.DetailResponse getCounselingHistoryDetail(String username, Long sessionId) {
        UserEntity user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        CounselingSessionEntity sessionEntity = counselingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + sessionId));

        if (!sessionEntity.getUser().getUserId().equals(username)) {
            throw new IllegalArgumentException("Access denied: This counseling session does not belong to the user.");
        }

        List<CounselingMessageEntity> messageEntities = counselingMessageRepository.findBySessionOrderByMessageIdAsc(sessionEntity);

        return CounselingDto.DetailResponse.fromEntity(sessionEntity, messageEntities);
    }

    @Transactional(readOnly = true)
    public Optional<CounselingDto.DetailResponse> getLatestCounselingHistoryDetail(String username) {
        UserEntity user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return counselingSessionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(sessionEntity -> {
                    List<CounselingMessageEntity> messageEntities = counselingMessageRepository.findBySessionOrderByMessageIdAsc(sessionEntity);
                    return CounselingDto.DetailResponse.fromEntity(sessionEntity, messageEntities);
                });
    }
}