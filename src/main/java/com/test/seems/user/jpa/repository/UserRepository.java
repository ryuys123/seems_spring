package com.test.seems.user.jpa.repository;

import com.test.seems.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, UserRepositoryCustom {
    //이 인터페이스를 통해서 jpa 가 제공하는 기본 메소드 사용 가능함
    // 추가된 메소드를 가진 MemberRepositoryCustom 메소드도 사용 가능함
    // MemberRepositoryCustomImpl 이 오버라이딩한 코드도 동적 바인딩되므로 사용가능함
    //    Optional<UserEntity> findByUserId(String userId); // userId로 사용자 조회
}
