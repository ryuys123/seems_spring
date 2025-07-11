package com.test.seems.faq.model.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.faq.jpa.entity.ReplyEntity;

@Data  // @Getter, @Setter, @ToString, @Equals, @HashCode 오버라이딩 까지 자동 코드 생성해 주는 어노테이션임
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reply {
    @NotBlank
    private int replyNo;
    private int faqNo;
    private int parentCommentNo;
    private int commentLevel;
    private int commentSeq;
    @NotBlank
    private String userid;
    @NotBlank
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date replyDate;


    //dto를 entity 로 변환하는 메소드
    public ReplyEntity toEntity(){
        return ReplyEntity.builder()
                .replyNo(replyNo)
                .faqNo(faqNo)
                .parentCommentNo(parentCommentNo)
                .commentLevel(commentLevel)
                .commentSeq(commentSeq)
                .userid(userid)
                .content(content)
                .replyDate(replyDate)
                .build();
    }
}
