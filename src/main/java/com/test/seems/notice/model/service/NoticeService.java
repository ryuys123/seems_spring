package com.test.seems.notice.model.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.notice.jpa.entity.NoticeEntity;
import com.test.seems.notice.jpa.repository.NoticeRepository;
import com.test.seems.notice.model.dto.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j   // Logger 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService  {
	// jpa 가 제공하는 기본 메소드를 사용하려면
	@Autowired
	private final NoticeRepository noticeRepository;

	// ArrayList<Notice> 리턴하는 메소드들이 사용하는 중복 코드는 별도의 메소드로 작성함
	private ArrayList<Notice> toList(Page<NoticeEntity> page) {
		ArrayList<Notice> list = new ArrayList<>();
		for (NoticeEntity noticeEntity : page) {
			list.add(noticeEntity.toDto());
		}
		return list;
	}


//	public ArrayList<Notice> selectTop3() {
//		//jpa 가 제공하는 메소드 사용한다면
//		// 최근 공지글 3개 조회이므로,
//		// 우선 전체 목록 조회해 옴 (공지번호 기준 내림차순정렬해서 조회함)
//		// 전체 목록에서 상위 3개만 골라내서 별도 리스트에 저장해서 리턴 처리함
//		// findAll(Sort.by(Sort.Direction.DESC, "noticeNo")) : List<NoticeEntity>
//
//		List<NoticeEntity> entityList = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "noticeNo"));
//		log.info(entityList.toString());  // 반환된 결과 로그로 출력 확인
//
//		//3개만 추출함
//		ArrayList<Notice> list = new ArrayList<>();
//		for(int index = 0; index < 3; index++) {
//			list.add(entityList.get(index).toDto());
//		}
//		return list;
//	}  //공지글 갯수가 많은 경우에는 bad code 임

	public List<Notice> selectTop3() {
		// jpa 제공하는 메소드 없음 => NoticeRepository 에 메소드 추가 작성함
		/* 실행 sql 문 :
		*  	SELECT *
			FROM (SELECT ROWNUM RNUM, NOTICENO, TITLE, NOTICEDATE
					FROM (SELECT * FROM NOTICE
							WHERE IMPORTANCE = 'N'
							ORDER BY NOTICEDATE DESC, NOTICENO DESC))
			WHERE RNUM BETWEEN 1 AND 3
		* */
//		PageRequest pageRequest = PageRequest.of(0, 3,  //0번째 페이지에서 3건을 가져오는 의미
//				Sort.by(Sort.Direction.DESC, "noticeDate").and(Sort.by(Sort.Direction.DESC, "noticeNo")));
//		List<NoticeEntity> entityList = noticeRepository.findByImportance("N", pageRequest);

		//메소드 이름 기반 쿼리 작성하는 기술이 jpa 핵심 기술임
		//위의 코드를 메소드 이름 변경으로 코드 간단하게 변경해 봄
		List<NoticeEntity> entityList = noticeRepository.findTop3ByImportanceOrderByNoticeDateDescNoticeNoDesc("N");

		List<Notice> list = new ArrayList<>();
		for (NoticeEntity noticeEntity : entityList) {
			list.add(noticeEntity.toDto());
		}
		return list;
	}


	public int selectListCount() {
		//jpa 가 제공하는 메소드 사용
		//count() : long
		//테이블의 전체 행 수를 반환함
		return (int)noticeRepository.count();
	}

	//스프링이 제공하는 페이지 처리용 클래스 사용함 : org.springframework.data.domain.Pageable
//	public ArrayList<Notice> selectList(Pageable pageable) {
//		// jpa 가 제공하는 메소드 사용
//		// findAll() : Entity 들이 반환됨 => select * from notice 자동 실행됨
//		// 페이지 단위로 list 조회를 원한다면, 스프링이 제공하는 Pageable 객체를 사용함
//		// findAll(Pageable 변수) : Page<entity> 반환됨 => 한 페이지의 리스트 정보가 들어있음
//		Page<NoticeEntity> page = noticeRepository.findAll(pageable);
//
//		//ArrayList<Notice> 로 변환 처리 필요함
//		ArrayList<Notice> list = new ArrayList<>();
//
//		//page 안의 NoticeEntity 를 Notice 변환해서 리스트에 추가 처리함
//		for (NoticeEntity entity : page) {
//			list.add(entity.toDto());
//		}
//		return list;
//	}

	public ArrayList<Notice> selectList(Pageable pageable) {
		List<NoticeEntity> entityList = noticeRepository.findAllByOrderByImportanceDescNoticeDateDesc(pageable).getContent();

		ArrayList<Notice> dtoList = new ArrayList<>();
		for (NoticeEntity entity : entityList) {
			dtoList.add(entity.toDto());
		}
		return dtoList;
	}


	public int selectSearchTitleCount(String keyword) {
		/* sql :
		* 	select count(*) from notice
			where title like '%' || #{ keyword } || '%'
		* */
		return noticeRepository.countByTitleContainingIgnoreCase(keyword);
	}


	public int selectSearchContentCount(String keyword) {
		/* sql :
		* 	select count(*) from notice
			where noticecontent like '%' || #{ keyword } || '%'
		* */
		return noticeRepository.countByContentContainingIgnoreCase(keyword);
	}


	public int selectSearchDateCount(LocalDate begin, LocalDate end) {
		/* sql :
		* 	select count(*) from notice
			where noticedate between #{ begin } and #{ end }
		* */
		return noticeRepository.countByNoticeDateBetween(begin, end);
	}


	public ArrayList<Notice> selectSearchTitle(String keyword, Pageable pageable) {
		/* sql :
			select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where title like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
		return toList(noticeRepository.findByTitleContainingIgnoreCaseOrderByImportanceDescNoticeDateDescNoticeNoDesc(keyword, pageable));
	}


	public ArrayList<Notice> selectSearchContent(String keyword, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticecontent like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
		return toList(noticeRepository.findByContentContainingIgnoreCaseOrderByImportanceDescNoticeDateDescNoticeNoDesc(keyword, pageable));
	}


	public ArrayList<Notice> selectSearchDate(LocalDate begin, LocalDate end, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticedate between #{ begin } and #{ end }
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
		return toList(noticeRepository.findByNoticeDateBetweenOrderByImportanceDescNoticeDateDescNoticeNoDesc(
				begin, end, pageable));
	}


	public Notice selectNotice(int noticeNo) {
		// jpa 제공 메소드 사용
		// findById(id) : Optional<T>
		// 엔티티에 등록된 id 를 사용해서 entity 1개를 조회함
		Optional<NoticeEntity> entityOptional = noticeRepository.findById(noticeNo);
		return entityOptional.get().toDto();
	}


	public void updateAddReadCount(int noticeNo) {
		/* sql :
			update notice
			set readcount = readcount + 1
			where noticeno = #{ no }
		* */
		// jpa 가 제공하는 메소드 사용
		// 1. 전달된 공지번호에 대한 공지글 하나 조회함
//		Optional<NoticeEntity> entity = noticeRepository.findById(noticeNo);
//		if (entity.isPresent()) {
//			NoticeEntity noticeEntity = entity.get();
//			noticeEntity.setReadCount(noticeEntity.getReadCount() + 1);
//			noticeRepository.save(noticeEntity);
//		}

		//람다식으로 바꾼다면
		noticeRepository.findById(noticeNo).ifPresent(entity -> {
			entity.setReadCount(entity.getReadCount() + 1);
			noticeRepository.save(entity);
		});
	}

	@Transactional
	public int insertNotice(Notice notice) {
		// jpa 가 제공하는 메소드 사용
		// save(entity) : entity
		// 성공하면 기록한 entity 가 리턴되고, 실패하면 null 리턴됨
		NoticeEntity savedEntity = noticeRepository.save(notice.toEntity());
		return savedEntity != null ? 1 : 0;
	}


	@Transactional
	public int deleteNotice(int noticeNo) {
		// jpa 가 제공하는 메소드 사용
		// deleteById(pk 로 지정된 컬럼에 대한 property) : void
		// 성공하면 리턴값 없음, 실패하면 에러 발생함
		try {
			noticeRepository.deleteById(noticeNo);
			return 1;
		}catch(Exception e) {
			log.error(e.getMessage());
			return 0;
		}
	}

	@Transactional
	public int updateNotice(Notice notice) {
		// jpa 가 제공하는 메소드 사용
		// save(entity) : savedEntity
		// 실패하면 null 리턴

		// 공지번호가 존재하면 update, 존재하지 않으면 실패로 간주함
		if (notice.getNoticeNo() == 0 || !noticeRepository.existsById(notice.getNoticeNo())) {
			return 0;
		}

		//대상 공지번호가 존재하면 수정 처리함
		NoticeEntity updatedEntity = noticeRepository.save(notice.toEntity());
		return updatedEntity != null ? 1 : 0;
	}

	//ajax test : last notice select one
	public Notice selectLast() {
		// 추가 메소드로 작성
		/* sql :
		*	select * from notice
			where noticeno = (select max(noticeno) from notice)
		* */
		Optional<NoticeEntity> entityOptional = noticeRepository.findTopByOrderByNoticeNoDesc();
		return entityOptional.isPresent() ? entityOptional.get().toDto() : null;
	}

	// 매일 자정에 중요공지 종료 처리
	@Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
	public void updateExpiredImportance() {
		doUpdateImportance();
	}

	// 서버 시작 시에도 실행되게 추가
	@PostConstruct
	public void initImportanceCheckOnStartup() {
		log.info("🕒 서버 시작 시 중요공지 상태 갱신 로직 실행");
		doUpdateImportance();
	}

	// 공통 로직 메서드로 분리
	private void doUpdateImportance() {
		LocalDate today = LocalDate.now();
		Date todaySql = Date.valueOf(today);

		List<NoticeEntity> expiredList =
				noticeRepository.findByImportanceAndImpEndDateBefore("Y", todaySql);

		for (NoticeEntity entity : expiredList) {
			entity.setImportance("N");
			noticeRepository.save(entity);
		}

		log.info("✅ 중요공지 'Y→N' 갱신된 개수: {}", expiredList.size());
	}

	// 대시보드용 최신 공지사항 조회 (긴급 우선, 없으면 일반)
	public Notice selectLatestNotice() {
		// 1. 긴급 공지사항 중 가장 최근 것 조회
		NoticeEntity urgentNotice = noticeRepository.findFirstByImportanceOrderByNoticeDateDescNoticeNoDesc("Y");
		
		// 2. 일반 공지사항 중 가장 최근 것 조회
		NoticeEntity normalNotice = noticeRepository.findFirstByImportanceOrderByNoticeDateDescNoticeNoDesc("N");
		
		// 3. 긴급 공지사항이 있으면 긴급 공지사항 반환, 없으면 일반 공지사항 반환
		if (urgentNotice != null) {
			log.info("대시보드용 최신 공지사항 조회: 긴급 공지사항 - {}", urgentNotice.getTitle());
			return urgentNotice.toDto();
		} else if (normalNotice != null) {
			log.info("대시보드용 최신 공지사항 조회: 일반 공지사항 - {}", normalNotice.getTitle());
			return normalNotice.toDto();
		} else {
			log.info("대시보드용 최신 공지사항 조회: 공지사항 없음");
			return null; // 공지사항이 없는 경우
		}
	}
}