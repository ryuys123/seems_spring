package com.test.seems.social.model.service;

import com.test.seems.social.model.dto.SocialUserDto;
import com.test.seems.social.model.service.provider.SocialOAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import com.test.seems.security.jwt.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Date;

import java.util.Map;
import com.test.seems.social.jpa.repository.SocialLoginRepository;
import com.test.seems.social.jpa.entity.SocialLoginEntity;

@Service
@RequiredArgsConstructor
public class SocialOAuthService {
    private final Map<String, SocialOAuthProvider> providerMap;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final SocialLoginRepository socialLoginRepository;

    public String getAuthorizationUrl(String provider) {
        return providerMap.get(provider.toUpperCase()).getAuthorizationUrl();
    }

    public SocialUserDto getUserInfo(String provider, String code, String state) {
        return providerMap.get(provider.toUpperCase()).getUserInfo(code, state);
    }

    // 회원가입
    public UserEntity registerUser(SocialUserDto dto) {
        UserEntity existing = userRepository.findByUserId(dto.getUserId());
        if (existing != null) {
            return existing;
        }
        UserEntity user = UserEntity.builder()
                .userId(dto.getUserId())
                .userPwd(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .userName(dto.getName())
                .profileImage(dto.getProfileImage())
                .email(dto.getEmail())
                .createdAt(new Date())
                .updatedAt(new Date())
                .status(1)
                .adminYn("N")
                .faceLoginEnabled(false)
                .build();
        return userRepository.save(user);
    }

    // 토큰 발급
    public String issueJwtToken(UserEntity user) {
        return jwtUtil.generateToken(user.toDto(), "access");
    }

    // 소셜 로그인(회원가입 되어 있으면 바로 토큰 발급)
    public String socialLoginOrSignup(SocialUserDto dto) {
        Optional<UserEntity> userOpt = socialLoginRepository
            .findByProviderAndSocialId(dto.getProvider(), dto.getSocialId())
            .map(SocialLoginEntity::getUser);

        UserEntity user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = registerUser(dto);
            // 소셜 연동 정보도 추가로 저장
            socialLoginRepository.save(
                SocialLoginEntity.builder()
                    .user(user)
                    .provider(dto.getProvider())
                    .socialId(dto.getSocialId())
                    .socialEmail(dto.getSocialEmail())
                    .linkedAt(new Date())
                    .build()
            );
        }
        return issueJwtToken(user);
    }

    // 회원가입, 토큰발급 등 추가 메서드 구현 가능
} 