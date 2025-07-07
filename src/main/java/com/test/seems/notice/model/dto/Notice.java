package com.test.seems.notice.model.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	private String noticeTitle;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") //날짜시간 직렬화 (Long 정수형 밀리초 출력) 해결 방법임
	private Date noticeDate;	// 날짜가 Jackson 을 사용시 Date 객체가 직렬화되어서 밀리초로 표시됨
	private String noticeWriter;
	private String noticeContent;
	private String originalFilePath;
	private String renameFilePath;
	private String importance;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private Date impEndDate;
	private int readCount;

	//dto를 entity 로 변환하는 메소드
	public NoticeEntity toEntity(){
		return NoticeEntity.builder()
				.noticeNo(noticeNo)
				.noticeTitle(noticeTitle)
				.noticeDate(noticeDate)
				.noticeWriter(noticeWriter)
				.noticeContent(noticeContent)
				.originalFilePath(originalFilePath)
				.renameFilePath(renameFilePath)
				.importance(importance)
				.impEndDate(impEndDate)
				.readCount(readCount)
				.build();
	}
}
