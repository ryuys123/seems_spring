package com.test.seems.log.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.test.seems.log.jpa.entity.LogEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Data  // @Getter, @Setter, @ToString, @Equals, @HashCode 오버라이딩 까지 자동 코드 생성해 주는 어노테이션임
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Log {
    @NotBlank
    private int logId;
    private String userId;
    @NotBlank
    private String action;
    @NotBlank
    private String severity;
    private String beforeData;
    private String afterData;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    //dto를 entity 로 변환하는 메소드
    public LogEntity toEntity(){
        return LogEntity.builder()
                .logId(logId)
                .userId(userId)
                .action(action)
                .severity(severity)
                .beforeData(beforeData)
                .afterData(afterData)
                .createdAt(createdAt)
                .build();
    }
}