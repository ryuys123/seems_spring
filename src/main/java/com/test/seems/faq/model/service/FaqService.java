package com.test.seems.faq.model.service;

import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.faq.jpa.repository.FaqRepository;
import com.test.seems.faq.model.dto.Faq;
import com.test.seems.faq.model.dto.Reply;
import com.test.seems.notice.model.dto.Notice;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j   // Logger 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@Service
@RequiredArgsConstructor
@Transactional
public class FaqService  {
    // jpa 가 제공하는 기본 메소드를 사용하려면
    @Autowired
    private final FaqRepository faqRepository;

    private final ReplyService ReplyService;

    // ArrayList<Faq> 리턴하는 메소드들이 사용하는 중복 코드는 별도의 메소드로 작성함
    private ArrayList<Faq> toList(Page<FaqEntity> page) {
        ArrayList<Faq> list = new ArrayList<>();
        for (FaqEntity faqEntity : page) {
            list.add(faqEntity.toDto());
        }
        return list;
    }

    private ArrayList<Faq> toList(List<FaqEntity> entities) {
        ArrayList<Faq> list = new ArrayList<>();
        for (FaqEntity faqEntity : entities) {
            list.add(faqEntity.toDto());
        }
        return list;
    }

    public int selectListCount() {
        return (int)faqRepository.count();
    }

    //사용자용 게시글 조회
    public ArrayList<Faq> selectList(Pageable pageable) {
        return toList(faqRepository.findAll(pageable));
    }

    // 사용자별 FAQ 목록 조회
    public ArrayList<Faq> selectListByUserid(String userid, int currentPage, int limit) {
        log.info("사용자별 FAQ 목록 조회: userid={}, page={}, limit={}", userid, currentPage, limit);
        
        // 페이징을 위한 Pageable 객체 생성
        Pageable pageable = PageRequest.of(currentPage - 1, limit, Sort.Direction.DESC, "faqNo");
        
        // 사용자별 FAQ 목록 조회
        Page<FaqEntity> page = faqRepository.findByUseridOrderByFaqNoDesc(userid, pageable);
        return toList(page);
    }

    // 관리자용 게시글 조회
    public ArrayList<Faq> selectListForAdmin(Pageable pageable) {
        log.info("✅ 관리자용 FAQ 정렬 로직 실행됨");

        Page<FaqEntity> page = faqRepository.findAllWithCustomSort(pageable);
        return toList(page);
    }

    public Faq selectFaq(int faqNo) {
        // jpa 제공 메소드 사용
        // findById(id) : Optional<T>
        // 엔티티에 등록된 id 를 사용해서 entity 1개를 조회함
        Optional<FaqEntity> entityOptional = faqRepository.findById(faqNo);
        return entityOptional.get().toDto();
    }

    public Faq selectLast() {
        // 추가 메소드로 작성
		/* sql :
		*	select * from notice
			where faqno = (select max(noticeno) from notice)
		* */
        Optional<FaqEntity> entityOptional = faqRepository.findTopByOrderByFaqNoDesc();
        return entityOptional.isPresent() ? entityOptional.get().toDto() : null;
    }

    public int insertFaq(Faq faq) {
        // jpa 가 제공하는 메소드 사용
        // save(entity) : entity
        // 성공하면 기록한 entity 가 리턴되고, 실패하면 null 리턴됨
        FaqEntity savedEntity = faqRepository.save(faq.toEntity());
        return savedEntity != null ? 1 : 0;
    }

    public int deleteFaq(int faqNo) {
        // jpa 가 제공하는 메소드 사용
        // deleteById(pk 로 지정된 컬럼에 대한 property) : void
        // 성공하면 리턴값 없음, 실패하면 에러 발생함
        try {
            faqRepository.deleteById(faqNo);
            return 1;
        }catch(Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    public int updateFaq(Faq faq) {
        // jpa 가 제공하는 메소드 사용
        // save(entity) : savedEntity
        // 실패하면 null 리턴

        // faq번호가 존재하면 update, 존재하지 않으면 실패로 간주함
        if (faq.getFaqNo() == 0 || !faqRepository.existsById(faq.getFaqNo())) {
            return 0;
        }

        //대상 faq번호가 존재하면 수정 처리함
        FaqEntity updatedEntity = faqRepository.save(faq.toEntity());
        return updatedEntity != null ? 1 : 0;

    }

    public void updateFaqStatusOnly(int faqNo, String status) {
        Optional<FaqEntity> entityOpt = faqRepository.findById(faqNo);
        if (entityOpt.isPresent()) {
            FaqEntity faq = entityOpt.get();
            faq.setStatus(status);
            faqRepository.save(faq);
        }
    }

    public void updateFaqStatusAndReDate(int faqNo, String status) {
        Optional<FaqEntity> entityOpt = faqRepository.findById(faqNo);
        if (entityOpt.isPresent()) {
            FaqEntity faq = entityOpt.get();
            faq.setStatus(status);
            faq.setReFaqDate(new Date(System.currentTimeMillis()));
            faqRepository.save(faq);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void autoCloseFaqs() {
        // 오늘 기준 7일 전 날짜 계산
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        Date targetDate = Date.valueOf(sevenDaysAgo);

        // 상태가 CLOSED가 아니고, reFaqDate가 7일 이상 지난 FAQ 조회
        List<FaqEntity> faqsToClose = faqRepository.findByStatusNotAndReFaqDateBefore("CLOSED", targetDate);

        int closedCount = 0;

        for (FaqEntity faq : faqsToClose) {
            List<Reply> replies = ReplyService.getRepliesByFaqNo(faq.getFaqNo());
            if (!replies.isEmpty()) {
                Reply last = replies.get(replies.size() - 1);
                if ("user001".equals(last.getUserid())) {
                    faq.setStatus("CLOSED");
                    faqRepository.save(faq);
                    closedCount++;
                }
            }
        }

        log.info("🔒 자동 종료된 FAQ 수: {}", closedCount);
        }

        // 서버 시작시에도 status 변경 실행
    @PostConstruct
    public void initAutoCloseOnStartup() {
        log.info("🚀 서버 시작 시 자동 FAQ 종료 작업 실행 중...");
        autoCloseFaqs();
    }

    // FAQ글 검색 관련 (관리자용) **********************************************************
    public int selectSearchTitleCount(String keyword) {
		/* sql :
		* 	select count(*) from notice
			where title like '%' || #{ keyword } || '%'
		* */
        return faqRepository.countByTitleContainingIgnoreCase(keyword);
    }


    public int selectSearchContentCount(String keyword) {
		/* sql :
		* 	select count(*) from notice
			where noticecontent like '%' || #{ keyword } || '%'
		* */
        return faqRepository.countByContentContainingIgnoreCase(keyword);
    }


    public int selectSearchDateCount(LocalDate begin, LocalDate end) {
		/* sql :
		* 	select count(*) from notice
			where noticedate between #{ begin } and #{ end }
		* */
        return faqRepository.countByFaqDateBetween(begin, end);
    }

    public int selectSearchStatusCount(String keyword) {
        return faqRepository.countByStatusContainingIgnoreCase(keyword);
    }


    public ArrayList<Faq> selectSearchTitle(String keyword, Pageable pageable) {
		/* sql :
			select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where title like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(faqRepository.findByTitleContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(keyword, pageable));
    }


    public ArrayList<Faq> selectSearchContent(String keyword, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticecontent like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(faqRepository.findByContentContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(keyword, pageable));
    }


    public ArrayList<Faq> selectSearchDate(LocalDate begin, LocalDate end, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticedate between #{ begin } and #{ end }
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(faqRepository.findByFaqDateBetweenOrderByFaqDateDescFaqNoDesc(
                begin, end, pageable));
    }

    public ArrayList<Faq> selectSearchStatus(String keyword, Pageable pageable) {
        return toList(faqRepository.findByStatusContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(keyword, pageable));
    }

}

