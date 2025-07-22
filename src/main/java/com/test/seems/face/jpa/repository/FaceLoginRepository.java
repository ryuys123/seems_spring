package com.test.seems.face.jpa.repository;

import com.test.seems.face.jpa.entity.FaceLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceLoginRepository extends JpaRepository<FaceLoginEntity, Long> {

    /**
     * 사용자 ID로 등록된 모든 페이스 정보 조회
     */
    List<FaceLoginEntity> findByUserId(String userId);

    /**
     * 사용자 ID와 페이스 이름으로 특정 페이스 정보 조회
     */
    Optional<FaceLoginEntity> findByUserIdAndFaceName(String userId, String faceName);

    /**
     * 사용자 ID로 등록된 페이스 개수 조회
     */
    long countByUserId(String userId);
}