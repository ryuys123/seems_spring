package com.test.seems.notice.jpa.repository;

// JPA 는 Entity 와 Repository 를 만들어서 사용하는 기술임
// JPA 의 Repository 는 반드시 JpaRepository 인터페이스를 상속받아서 후손 인터페이스로 만듦
// 제네릭스는 반드시 <엔티티클래스명, @id 프로퍼티의 클래스자료형> 명시함
// MyBatis 의 SqlSessionTemplate 과 같은 역할을 수행함, Mapper 인터페이스와 같음
// @Repository 어노테이션 반드시 표시해야 함 => 자동 등록됨 => Service 에서 사용할 수 있게 됨

import com.test.seems.notice.jpa.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    // jpa 가 제공하는 기본 메소드를 사용할 수 있게 됨 (sql 구문 자동 생성 실행함)

    // jpa 가 제공하지 않는 메소드일 때는 추가 작성 선언함

    // 최근 공지글 3개 조회
    // 반환자료형이 List<NoticeEntity> 이면, 단순 결과 목록만 반환됨 (페이징 정보 없음)
    //List<NoticeEntity> findByImportance(String importance, Pageable pageable);
    // 내부 코드는 자동 구현됨, 메소드 이름을 보고 sql 구문을 자동 생성함, 메소드 선언만 하면 됨
    List<NoticeEntity> findTop3ByImportanceOrderByNoticeDateDescNoticeNoDesc(String importance);

    //제목 키워드 검색 관련 목록 갯수 조회용
    int countByTitleContainingIgnoreCase(String keyword);
    //제목 검색 목록 조회용 (페이지 적용)
    Page<NoticeEntity> findByTitleContainingIgnoreCaseOrderByImportanceDescNoticeDateDescNoticeNoDesc(String keyword, Pageable pageable);

    //내용 키워드 검색 관련 목록 갯수 조회용
    int countByContentContainingIgnoreCase(String keyword);
    //내용 검색 목록 조회용 (페이지 적용)
    Page<NoticeEntity> findByContentContainingIgnoreCaseOrderByImportanceDescNoticeDateDescNoticeNoDesc(String keyword, Pageable pageable);

    //날짜 검색 관련 목록 갯수 조회용
    int countByNoticeDateBetween(LocalDate begin, LocalDate end);
    //날짜 검색 목록 조회용 (페이지 적용)
    Page<NoticeEntity> findByNoticeDateBetweenOrderByImportanceDescNoticeDateDescNoticeNoDesc(LocalDate begin, LocalDate end, Pageable pageable);

    // 최근 공지글 1개 조회 (가장 큰 공지번호에 대한 공지글 1개 조회)
    Optional<NoticeEntity> findTopByOrderByNoticeNoDesc();
}

/* JpaRepository 가 제공하는 기본 메소드 정리
*  findAll()  => select * from 테이블명
*  findById(id)  => select * from 테이블명 where @id로 지정된 컬럼 = 전달값
*  save(entity) => insert 쿼리 실행, update 쿼리 실행
*  delete(entity) => delete 쿼리 실행
*  count() => select count(*) from 테이블명
*  findAll(Sort sort)  => select * from 테이블명 order by 정렬기준 정렬방식
*  findAll(Pageable pageable)   => select * from 테이블명 where rownum between startrow and endrow
*  existsById(id)
* */
