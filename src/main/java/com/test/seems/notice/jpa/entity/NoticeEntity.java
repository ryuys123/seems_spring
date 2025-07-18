package com.test.seems.notice.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.notice.model.dto.Notice;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "TB_NOTICES")  //매핑할 테이블 이름 지정함, NOTICE 테이블을 자동으로 만들어 주기도 하는 어노테이션임
@Entity     // jpa 가 관리함, db 테이블과 dto 클래스를 매핑하기 위해 필요함
public class NoticeEntity {
    @Id     // jpa 가 엔티티를 관리할 때 식별할 id 생성 용도의 어노테이션임
    //@GeneratedValue(strategy = GenerationType.IDENTITY)  //테이블 자동으로 만들어질떼 기본키 지정하는 어노테이션임
    @Column(name="NOTICE_NO", nullable=false)
    private int noticeNo;
    @Column(name="TITLE", nullable=false)
    private String title;
    @Column(name="USER_ID", nullable=false)
    private String userid;
    @Column(name="CONTENT", nullable=false)
    private String content;
    @Column(name="READCOUNT", columnDefinition = "0")
    private int readCount;
    @Column(name="ORIGINAL_FILEPATH")
    private String originalFilePath;
    @Column(name="RENAME_FILEPATH")
    private String renameFilePath;
    @Column(name="IMPORTANCE", columnDefinition = "N")
    private String importance;
    @Column(name="IMP_END_DATE")
    private Date impEndDate;
    @Column(name="NOTICE_DATE", nullable=false)
    private Date noticeDate;


//    @Column(name="IMPORTANCE", columnDefinition = "N")
//    private String importance;


    @PrePersist   // jpa 리포지터리로 넘어가기 전에 (sql 에 적용하기 전) 작동되는 어노테이션임
    public void prePersist() {
        noticeDate = new java.sql.Date(System.currentTimeMillis());
        impEndDate = new java.sql.Date(System.currentTimeMillis());
    }

    //controller 는 dto 를 받아서 취급함
    //repository 는 entity 를 사용함
    //controller --> service --> repository 로 연결되는 구조임
    //controller, dto --> service : dto 를 entity 로 바꿈 --> repository, entity
    //controller, dto <-- service : entity 를 dto 로 바꿈 <-- repository, entity

    //entity 를 dto 로 변환하는 메소드
    public Notice toDto(){
        return Notice.builder()
                .noticeNo(noticeNo)
                .title(title)
                .userid(userid)
                .content(content)
                .readCount(readCount)
                .originalFilePath(originalFilePath)
                .renameFilePath(renameFilePath)
                .importance(importance)
                .impEndDate(impEndDate)
                .noticeDate(noticeDate)
                .build();
    }
}