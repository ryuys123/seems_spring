package com.test.seems.user.jpa.repository;

import com.test.seems.user.jpa.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepositoryCustom {
    

    @Query(value = "UPDATE TB_USERS SET STATUS = :status WHERE USER_ID = :userId", nativeQuery = true)
    int modifyUserStatus(@Param("userId") String userId, @Param("status") int status);

    // 전화번호 인증 관련
    UserEntity findByPhone(String phone);

    //관리자용 검색 관련
    long countByUserId(String keyword);
    long countByUserNameContaining(String keyword);
    long countByCreatedAtBetween(java.util.Date begin, java.util.Date end);
    long countByStatus(int keyword);

    List<UserEntity> findByUserIdEquals(String keyword, Pageable pageable);
    List<UserEntity> findByUserNameContaining(String keyword, Pageable pageable);
    List<UserEntity> findByCreatedAtBetween(java.util.Date begin, java.util.Date end, Pageable pageable);
    List<UserEntity> findByStatus(int keyword, Pageable pageable);

}
