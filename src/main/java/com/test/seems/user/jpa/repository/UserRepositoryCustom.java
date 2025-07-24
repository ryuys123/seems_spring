package com.test.seems.user.jpa.repository;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepositoryCustom {

    // 전화번호 인증 관련
    UserEntity findByPhone(String phone);

}
