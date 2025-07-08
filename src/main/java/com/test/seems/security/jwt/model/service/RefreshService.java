package com.test.seems.security.jwt.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.security.jwt.jpa.entity.RefreshToken;
import com.test.seems.security.jwt.jpa.repository.RefreshRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RefreshService {
    private final RefreshRepository refreshRepository;

    @Transactional
    public void saveRefresh(RefreshToken refreshToken) {
        refreshRepository.save(refreshToken);  // jpa 가 제공하는 메소드 사용
    }
    
    @Transactional
    public void deleteRefresh(String id) {
        refreshRepository.deleteById(id);  // jpa 가 제공하는 메소드 사용
    }

    @Transactional
    public int updateRefreshToken(String id, String tokenValue) {
        return refreshRepository.updateTokenById(id, tokenValue);
    }

    public String selectId(String userId, String tokenValue) {
        return refreshRepository.findByUserIdAndTokenValue(userId, tokenValue);
    }

    public String selectTokenValue(String userId) {
        return refreshRepository.findTokenValueByUserId(userId);
    }
}
