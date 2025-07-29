package com.test.seems.faq.jpa.repository;

import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.notice.jpa.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Integer> {

    // 최근 faq글 1개 조회 (가장 큰 faq글 번호 1개 조회)
    Optional<FaqEntity> findTopByOrderByFaqNoDesc();

    // 상태가 CLOSED가 아닌 FAQ들 중에서, 7일 이상 지난 항목만 가져오기
    List<FaqEntity> findByStatusNotAndReFaqDateBefore(String status, Date date);

    @Query("SELECT f FROM FaqEntity f " +
            "ORDER BY " +
            "CASE f.status " +
            "   WHEN 'PENDING' THEN 0 " +
            "   WHEN 'ANSWERED' THEN 1 " +
            "   WHEN 'CLOSED' THEN 2 " +
            "   ELSE 99 " +
            "END, " +
            "f.faqNo ASC")
    Page<FaqEntity> findAllWithCustomSort(Pageable pageable);

    // FAQ글 검색 관련 (관리자용) **********************************************************
    //제목 키워드 검색 관련 목록 갯수 조회용
    int countByTitleContainingIgnoreCase(String keyword);
    //제목 검색 목록 조회용 (페이지 적용)
    Page<FaqEntity> findByTitleContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(String keyword, Pageable pageable);

    //내용 키워드 검색 관련 목록 갯수 조회용
    int countByContentContainingIgnoreCase(String keyword);
    //내용 검색 목록 조회용 (페이지 적용)
    Page<FaqEntity> findByContentContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(String keyword, Pageable pageable);

    //날짜 검색 관련 목록 갯수 조회용
    int countByFaqDateBetween(LocalDate begin, LocalDate end);
    //날짜 검색 목록 조회용 (페이지 적용)
    Page<FaqEntity> findByFaqDateBetweenOrderByFaqDateDescFaqNoDesc(LocalDate begin, LocalDate end, Pageable pageable);

    //답변상태 검색 관련 목록 갯수 조회용
    int countByStatusContainingIgnoreCase(String keyword);
    //내용 검색 목록 조회용 (페이지 적용)
    Page<FaqEntity> findByStatusContainingIgnoreCaseOrderByFaqDateDescFaqNoDesc(String keyword, Pageable pageable);

    // 사용자별 FAQ 목록 조회 (페이징 적용)
    Page<FaqEntity> findByUseridOrderByFaqNoDesc(String userid, Pageable pageable);

//    // 사용자별 FAQ 목록 조회 (최근활동용)
//    List<FaqEntity> findByUseridOrderByFaqDateDesc(String userid);
}



