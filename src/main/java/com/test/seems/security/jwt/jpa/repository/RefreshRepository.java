package com.test.seems.security.jwt.jpa.repository;

import com.test.seems.security.jwt.jpa.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, String> {

    // userId로 tokenValue 조회
    @Query("SELECT r.tokenValue FROM RefreshToken r WHERE r.userId = :userId")
    String findTokenValueByUserId(@Param("userId") String userId);

    // userId 와 refreshToken 값으로 ID 조회
    @Query("SELECT r.id FROM RefreshToken r WHERE r.userId = :userId AND r.tokenValue = :tokenValue")
    String findByUserIdAndTokenValue(@Param("userId") String userId, @Param("tokenValue") String tokenValue);

    // id 로 토큰 갱신 (JPQL 로 update 쿼리 작성시에는 반드시 @Modifying 필수 표기할 것)
    @Modifying  // 필수
    @Query("UPDATE RefreshToken r SET r.tokenValue = :tokenValue WHERE r.id = :id")
    int updateTokenById(@Param("id") String id, @Param("tokenValue") String tokenValue);

}
