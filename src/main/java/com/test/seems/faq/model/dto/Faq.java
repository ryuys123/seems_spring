package com.test.seems.faq.model.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.faq.jpa.entity.FaqEntity;

@Data  // @Getter, @Setter, @ToString, @Equals, @HashCode 오버라이딩 까지 자동 코드 생성해 주는 어노테이션임
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Faq {
    @NotBlank
    private int faqNo;
    @NotBlank
    private String userid;
    @NotBlank
    private String title;
    @NotBlank
    private String category;
    @NotBlank
    private String content;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date faqDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date reFaqDate;


    //dto를 entity 로 변환하는 메소드
    public FaqEntity toEntity(){
        return FaqEntity.builder()
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
