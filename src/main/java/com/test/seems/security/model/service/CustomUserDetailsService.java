package com.test.seems.security.model.service;

import lombok.extern.slf4j.Slf4j;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 만드는 이유 : DB에서 사용자 정보를 조회하여 스프링 시큐리티가 사용할 수 있게 하기 위함
// 역할 : 사용자 정보 로딩 및 UserDetails로 반환
// 사용 위치 : 보통 시큐리티 설정 클래스에서 AuthenticationManagerBuilder에 등록하여 사용
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    // UserDetailsService는 스프링 시큐리티가 인증(Authentication) 과정을 수행할 때
    // 사용자 정보를 조회하는 데 사용하는 인터페이스이다.
    // 기본 UserDetailsService는 DB나 사용자 정보를 알지 못하기 때문에 다음이 필요하다:
    // 1. 회원 정보를 데이터베이스에서 조회
    // 2. 해당 정보를 UserDetails 객체로 변환
    // 3. 스프링 시큐리티가 해당 객체를 이용해 로그인 처리를 수행

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 사용자 조회
        // 2. UserDetails 타입으로 반환

        UserEntity userEntity = userRepository.findByUserId(username);
        if (userEntity == null) {
            System.out.println("사용자를 찾을 수 없습니다: " + username);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        log.info("DB에서 조회된 사용자: " + userEntity);
        log.info("DB 비밀번호: {}", userEntity.getUserPwd());
        log.info("DB 비밀번호가 BCrypt 형태인지: {}", 
                 userEntity.getUserPwd() != null && userEntity.getUserPwd().startsWith("$2a$") ? "YES" : "NO");

        return User.builder()
                .username(userEntity.getUserId())
                .password(userEntity.getUserPwd())
                .roles(userEntity.getAdminYn().equals("Y") ? "ADMIN" : "USER")
                .build();
    }
}
