package com.test.seems.counseling.model.service;

//import com.test.seems.counseling.*;
import com.test.seems.counseling.model.dto.CounselingDto;
import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import com.test.seems.counseling.jpa.repository.CounselingMessageRepository;
import com.test.seems.counseling.jpa.repository.CounselingMessageRepository;
import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import com.test.seems.counseling.jpa.repository.CounselingSessionRepository;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselingService {

    private final CounselingSessionRepository counselingSessionRepository;
    private final CounselingMessageRepository counselingMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public CounselingDto.HistoryResponse saveCounselingHistory(String username, CounselingDto.CreateRequest request) {
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
        for (CounselingDto.MessageDto messageDto : request.getMessages()) {
            com.test.seems.counseling.jpa.entity.CounselingMessageEntity messageEntity = CounselingMessageEntity.builder()
                    .session(sessionEntity) // 기존 또는 새로 생성된 세션에 연결
                    .sender(messageDto.getType().toUpperCase()) // USER, AI
                    .messageType("TEXT") // 현재는 텍스트만 지원
                    .messageContent(messageDto.getText())
                    .messageTime(new Date()) // 현재 시간으로 메시지 시간 설정
                    .build();
            counselingMessageRepository.save(messageEntity);
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
}