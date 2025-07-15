package com.test.seems.faq.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.faq.model.dto.Faq;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "TB_INQUIRIES")  //매핑할 테이블 이름
@Entity     // jpa 가 관리함, db 테이블과 dto 클래스를 매핑하기 위해 필요함
public class FaqEntity {
    @Id     // jpa 가 엔티티를 관리할 때 식별할 id 생성 용도의 어노테이션임
    //@GeneratedValue(strategy = GenerationType.IDENTITY)  //테이블 자동으로 만들어질떼 기본키 지정하는 어노테이션임
    @Column(name="INQUIRY_ID", nullable=false)
    private int faqNo;
    @Column(name="USER_ID", nullable=false)
    private String userid;
    @Column(name="TITLE", nullable=false)
    private String title;
    @Column(name="CATEGORY", nullable=false)
    private String category;
    @Column(name="CONTENT", nullable=false)
    private String content;
    @Column(name="STATUS", nullable=false)
    private String status;
    @Column(name="CREATED_AT", nullable=false)
    private Date faqDate;
    @Column(name="LAST_ACTIVITY_AT")
    private Date reFaqDate;

    @PrePersist   // jpa 리포지터리로 넘어가기 전에 (sql 에 적용하기 전) 작동되는 어노테이션임
    public void prePersist() {
        faqDate = new java.sql.Date(System.currentTimeMillis());
        reFaqDate = new java.sql.Date(System.currentTimeMillis());
    }

    //controller 는 dto 를 받아서 취급함
    //repository 는 entity 를 사용함
    //controller --> service --> repository 로 연결되는 구조임
    //controller, dto --> service : dto 를 entity 로 바꿈 --> repository, entity
    //controller, dto <-- service : entity 를 dto 로 바꿈 <-- repository, entity

    //entity 를 dto 로 변환하는 메소드
    public Faq toDto(){
        return Faq.builder()
                .faqNo(faqNo)
                .title(title)
                .userid(userid)
                .category(category)
                .content(content)
                .faqDate(faqDate)
                .reFaqDate(reFaqDate)
                .status(status)
                .build();
    }
}