package com.test.seems.user.jpa.repository;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, UserRepositoryCustom {
    //이 인터페이스를 통해서 jpa 가 제공하는 기본 메소드 사용 가능함
    // 추가된 메소드를 가진 MemberRepositoryCustom 메소드도 사용 가능함
    // MemberRepositoryCustomImpl 이 오버라이딩한 코드도 동적 바인딩되므로 사용가능함
    UserEntity findByUserId(String userId); // userId로 사용자 조회
    UserEntity findByUserNameAndPhone(String userName, String phone);
  
      @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.userId = :userId")
    int modifyUserStatus(@Param("userId") String userId, @Param("status") int status);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.userId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    int countByKeyword(@Param("keyword") String keyword);
    int countByStatus(int status);
    int countByCreatedAtBetween(Date begin, Date end);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.userId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    List<UserEntity> findByStatusEquals(int status, Pageable pageable);
    List<UserEntity> findByCreatedAtBetween(Date start, Date end, Pageable pageable);
}


