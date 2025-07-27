package com.test.seems.social.jpa.repository;

import com.test.seems.social.jpa.entity.SocialLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLoginEntity, Long> {
    
    // 특정 소셜 제공자와 소셜 ID로 연동 정보 조회
    Optional<SocialLoginEntity> findByProviderAndSocialId(String provider, String socialId);
    
    // 특정 사용자의 소셜 연동 정보 조회
    Optional<SocialLoginEntity> findByUser_UserIdAndProvider(String userId, String provider);
    
    // 소셜 이메일로 연동 정보 조회
    Optional<SocialLoginEntity> findBySocialEmail(String socialEmail);
    
    // 특정 소셜 제공자와 이메일로 연동 정보 조회
    Optional<SocialLoginEntity> findByProviderAndSocialEmail(String provider, String socialEmail);
    
    // 특정 사용자의 모든 소셜 연동 정보 조회
    List<SocialLoginEntity> findByUser_UserId(String userId);
    
    // 특정 사용자의 모든 소셜 연동 정보 삭제
    void deleteByUser_UserId(String userId);
} 