package com.test.seems.counseling.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class CounselingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private String type; // user, ai
        private String text;
    }

    /**
     * 상담 내용 저장을 위한 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private Long sessionId; // 기존 세션 ID (선택 사항)
        private String topic; // 상담 주제 (예: '새로운 상담')
        private String method; // 상담 방식 (TEXT, VOICE)
        private List<MessageDto> messages; // 프론트에서 받은 messages 배열
    }

    /**
     * 상담 기록 목록 조회를 위한 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class HistoryResponse {
        private Long sessionId;
        private String topic;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createdAt;

        public static HistoryResponse fromEntity(CounselingSessionEntity entity) {
            return new HistoryResponse(entity.getSessionId(), entity.getTopic(), entity.getCreatedAt());
        }
    }

    /**
     * 특정 상담 기록 상세 조회를 위한 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class DetailResponse {
        private Long sessionId;
        private String topic;
        private String method;
        private List<MessageDto> messages;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createdAt;

        public static DetailResponse fromEntity(CounselingSessionEntity sessionEntity, List<CounselingMessageEntity> messageEntities) {
            List<MessageDto> messages = messageEntities.stream()
                    .map(msg -> new MessageDto(msg.getSender().toLowerCase(), msg.getMessageContent()))
                    .collect(java.util.stream.Collectors.toList());
            return new DetailResponse(sessionEntity.getSessionId(), sessionEntity.getTopic(), sessionEntity.getMethod(), messages, sessionEntity.getCreatedAt());
        }
    }
}