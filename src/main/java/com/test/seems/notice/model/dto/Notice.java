package com.test.seems.notice.model.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.seems.notice.jpa.entity.NoticeEntity;

@Data  // @Getter, @Setter, @ToString, @Equals, @HashCode 오버라이딩 까지 자동 코드 생성해 주는 어노테이션임
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice {
	@NotBlank
	private int noticeNo;
	@NotBlank
	private String title;
	@NotBlank
	private String userid;
	@NotBlank
	private String content;
	private int readCount;
	private String originalFilePath;
	private String renameFilePath;
	private String importance;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private Date impEndDate;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private Date noticeDate;


	//dto를 entity 로 변환하는 메소드
	public NoticeEntity toEntity(){
		return NoticeEntity.builder()
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
