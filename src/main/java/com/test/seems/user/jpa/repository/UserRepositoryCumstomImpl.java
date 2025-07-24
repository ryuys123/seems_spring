package com.test.seems.user.jpa.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.test.seems.user.jpa.entity.QUserEntity.userEntity;

@Repository
@RequiredArgsConstructor  //final 필드에 대해 생성자 주입 자동 생성용 어노테이션 (Lombok)
public class UserRepositoryCumstomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;  //QueryDSL 의 핵심 객체임
    private final EntityManager entityManager;  //queryDSL 에서 JPQL/Native Query 사용을 위해 의존성 추가함

    //queryDSL 은 Q엔티티클래스 사용함 => import static 을 사용하면 별도로 선언하지 않아도 됨
    //private QUserEntity qUserEntity = QUserEntity.userEntity;

    

    /**
     * 로그인 가능 여부 상태 변경 (JPQL Native Query 사용)
     */
//    @Override
//    public int modifyUserStatus(String userId, int status) {
//        // @Query가 우선이므로 이 메서드는 실행되지 않음
//        // 아무 내용이나 넣어도 됨
//        return 0;
//    }

    // 전화번호 인증 관련
    @Override
    public UserEntity findByPhone(String phone) {
        return queryFactory
                .selectFrom(userEntity)
                .where(userEntity.phone.eq(phone))
                .fetchOne(); // 단일 결과 반환 (없으면 null, 2개 이상이면 예외)
    }

//    /**
//     * userId 에 특정 키워드가 포함된 회원수 조회
//     */
//    @Override
//    public long countByUserId(String keyword) {
//        return queryFactory
//                .select(Wildcard.count)  // select count(*) 역할을 하는 QueryDSL 전용 상수
//                .from(userEntity)  // from user
//                .where(userEntity.userId.eq(keyword))
//                .fetchOne();  // 카운트한 결과 한 개 반환
//    }
//
//    /**
//     * userName 에 특정 키워드가 포함된 회원수 조회
//     */
//    @Override
//    public long countByUserNameContaining(String keyword) {
//        return queryFactory
//                .select(Wildcard.count)  // select count(*) 역할을 하는 QueryDSL 전용 상수
//                .from(userEntity)  // from user
//                .where(userEntity.userName.containsIgnoreCase(keyword))  // where username LIKE %keyword% (대소문자 무시)
//                .fetchOne();  // 카운트한 결과 한 개 반환
//    }
//
//    /**
//     * 회원가입 등록일 기간으로 회원 수 조회
//     * */
//    @Override
//    public long countByCreatedAtBetween(Date begin, Date end) {
//        return queryFactory
//                .select(Wildcard.count)  // select count(*) 역할을 하는 QueryDSL 전용 상수
//                .from(userEntity)  // from user
//                .where(userEntity.createdAt.between((java.sql.Date) begin, (java.sql.Date) end))  // where created_at between :begin and :end
//                .fetchOne();  // 카운트한 결과 한 개 반환
//    }
//
//    /**
//     * 계정 상태 기준으로 회원 수 조회
//     * */
//    @Override
//    public long countByStatus(int keyword) {
//        return queryFactory
//                .select(Wildcard.count)  // select count(*) 역할을 하는 QueryDSL 전용 상수
//                .from(userEntity)  // from user
//                .where(userEntity.status.eq(keyword))  // where status = :keyword
//                .fetchOne();  // 카운트한 결과 한 개 반환
//    }
//
//    /**
//     * userid 로 회원 정보 검색 (페이징 적용 조회)
//     * */
//    @Override
//    public List<UserEntity> findByUserIdEquals(String keyword, Pageable pageable) {
//        return queryFactory
//                .selectFrom(userEntity)  //select * from user
//                .where(userEntity.userId.eq(keyword))
//                .offset(pageable.getOffset())  // 시작행 지정 (출력할 페이지숫자 - 1)
//                .limit(pageable.getPageSize())  // 페이지 크기 (한 페이지에 출력할 목록 갯수)
//                .fetch();  //결과 리스트 반환
//    }
//
//    /**
//     * username 으로 회원 정보 검색 (페이징 적용 조회)
//     * */
//    @Override
//    public List<UserEntity> findByUserNameContaining(String keyword, Pageable pageable) {
//        return queryFactory
//                .selectFrom(userEntity)  //select * from user
//                .where(userEntity.userName.eq(keyword))
//                .offset(pageable.getOffset())  // 시작행 지정 (출력할 페이지숫자 - 1)
//                .limit(pageable.getPageSize())  // 페이지 크기 (한 페이지에 출력할 목록 갯수)
//                .fetch();  //결과 리스트 반환
//    }
//
//    /**
//     * 가입날짜별 검색 (페이징 적용 조회)
//     * */
//    @Override
//    public List<UserEntity> findByCreatedAtBetween(Date begin, Date end, Pageable pageable) {
//        return queryFactory
//                .selectFrom(userEntity)  //select * from user
//                .where(userEntity.createdAt.between((java.sql.Date) begin, (java.sql.Date) end))  //where enroll_date between :begin and :end
//                .offset(pageable.getOffset())  // 시작행 지정 (출력할 페이지숫자 - 1)
//                .limit(pageable.getPageSize())  // 페이지 크기 (한 페이지에 출력할 목록 갯수)
//                .fetch();  //결과 리스트 반환
//    }
//
//    /**
//     * status 상태별로 검색 (페이징 적용 조회)
//     * */
//    @Override
//    public List<UserEntity> findByStatus(int keyword, Pageable pageable) {
//        return queryFactory
//                .selectFrom(userEntity)  //select * from user
//                .where(userEntity.status.eq(keyword))  //where status = :keyword
//                .offset(pageable.getOffset())  // 시작행 지정 (출력할 페이지숫자 - 1)
//                .limit(pageable.getPageSize())  // 페이지 크기 (한 페이지에 출력할 목록 갯수)
//                .fetch();  //결과 리스트 반환
//    }

}