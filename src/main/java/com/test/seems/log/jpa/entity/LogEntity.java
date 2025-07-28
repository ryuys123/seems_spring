package com.test.seems.log.jpa.entity;

import com.test.seems.log.model.dto.Log;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "TB_SYSTEM_LOGS")  //매핑할 테이블 이름 지정함, NOTICE 테이블을 자동으로 만들어 주기도 하는 어노테이션임
@Entity     // jpa 가 관리함, db 테이블과 dto 클래스를 매핑하기 위해 필요함
public class LogEntity {
    @Id     // jpa 가 엔티티를 관리할 때 식별할 id 생성 용도의 어노테이션임
    //@GeneratedValue(strategy = GenerationType.IDENTITY)  //테이블 자동으로 만들어질떼 기본키 지정하는 어노테이션임
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_seq_gen")
    @SequenceGenerator(
            name = "log_seq_gen",
            sequenceName = "SEQ_SYSTEM_LOGS_LOG_ID",
            allocationSize = 1
    )    @Column(name = "LOG_ID", nullable = false)
    private int logId;
    @Column(name = "USER_ID")
    private String userId;
    @Column(name = "ACTION", nullable = false)
    private String action;
    @Column(name = "SEVERITY", nullable = false)
    private String severity;
    @Column(name = "BEFORE_DATA")
    private String beforeData;
    @Column(name = "AFTER_DATA")
    private String afterData;
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

//    @Column(name="IMPORTANCE", columnDefinition = "N")
//    private String importance;


    @PrePersist   // jpa 리포지터리로 넘어가기 전에 (sql 에 적용하기 전) 작동되는 어노테이션임
    public void prePersist() {
        createdAt = LocalDateTime.now();  // 시분초까지 나오게
    }

    //controller 는 dto 를 받아서 취급함
    //repository 는 entity 를 사용함
    //controller --> service --> repository 로 연결되는 구조임
    //controller, dto --> service : dto 를 entity 로 바꿈 --> repository, entity
    //controller, dto <-- service : entity 를 dto 로 바꿈 <-- repository, entity

    //entity 를 dto 로 변환하는 메소드
    public Log toDto() {
        return Log.builder()
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