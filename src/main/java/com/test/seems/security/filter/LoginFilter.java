package com.test.seems.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.security.jwt.jpa.entity.RefreshToken;
import com.test.seems.security.jwt.model.service.RefreshService;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    // LoginFilter 는 Spring 컨테이너가 관리하는 컴포넌트가 아님 (@Bean, @Service, @Repository, @Component, @Controller 등)
    // 필터에서 직접 의존성 주입 못함
    // SecurityConfig 에서 의존성 주입해서 넘겨 받아야 함

    private final JWTUtil jwtUtil;  // 로그인 성공시 토큰 생성을 위해 필요
    private final UserRepository userRepository;  // 전송온 아이디로 회원 정보 조회를 위해 필요
    private final RefreshService refreshService;   // 리프레시토큰 db table 에 저장을 위해 필요
    // save (insert query) 실행 후 트랜잭션(commit, rollback) 처리를 위해 서비스를 사용함 (repository 는 트랜잭션 처리 못 함)

    // 생성자에 PasswordEncoder 추가
    private final PasswordEncoder passwordEncoder;

    // 매개변수 있는 생성자 직접 작성해야 함
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
                       UserRepository userRepository, RefreshService refreshService, PasswordEncoder passwordEncoder) {
        this.setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshService = refreshService;
        this.passwordEncoder = passwordEncoder;  // 추가
        this.setFilterProcessesUrl("/login");   // 로그인 url 에 대한 앤드포인트 설정
    }

    // 로그인 요청 처리용 메소드 (전송온 아이디, 패스워드 확인용)
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException{
        String userId = null;
        String userPwd = null;

        // UUID 기반 요청인지 확인 (회원 테이블에 ID 컬럼(varchar2) 에 UUID (java.util.UUID) 를 기록 저장한 경우)
        if (request.getAttribute("userId") != null && request.getAttribute("userPwd") != null) {
            userId = (String) request.getAttribute("userId");
            userPwd = (String) request.getAttribute("userPwd");
            log.info("UUID-based Login Request Detected : userId={}, userPwd={}", userId, userPwd);

            //비밀번호 검증 건너뛰어도 됨
            return this.getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(userId, userPwd));
        }

        // 일반 로그인 처리
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> requestBody = mapper.readValue(request.getInputStream(), Map.class);
            log.info("requestBody={}", requestBody);
            userId = requestBody.get("userId");
            userPwd = requestBody.get("userPwd");
        } catch (IOException e) {
            throw new RuntimeException("요청 데이터를 읽을 수 없습니다.", e);
        }

        if (userId == null || userPwd == null) {
            throw new RuntimeException("아이디 또는 비밀번호가 전달되지 않았습니다..");
        }

        // 디버깅을 위한 로그 추가
        log.info("로그인 시도 - userId: {}, userPwd: {}", userId, userPwd);
        log.info("userPwd 길이: {}", userPwd != null ? userPwd.length() : 0);
        log.info("userPwd가 해싱된 형태인지 확인: {}", userPwd != null && userPwd.startsWith("$2a$") ? "YES" : "NO");
        
        // DB에서 사용자 정보 조회하여 비밀번호 상태 확인
        UserEntity dbUser = userRepository.findByUserId(userId);
        if (dbUser != null) {
            log.info("DB 사용자 비밀번호: {}", dbUser.getUserPwd());
            log.info("DB 비밀번호가 BCrypt 형태인지: {}", 
                     dbUser.getUserPwd() != null && dbUser.getUserPwd().startsWith("$2a$") ? "YES" : "NO");
        }

        // Spring Security의 DaoAuthenticationProvider가 자동으로 비밀번호 검증을 처리함
        // DB에 저장된 해싱된 비밀번호와 클라이언트에서 전송된 평문 비밀번호를 비교
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, userPwd);
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

    // 로그인 성공시 처리할 내용 작성용 메소드 (db 조회, 토큰 생성)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                      FilterChain filterChain, Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공 로직 실행");
        String userId = authentication.getName();  // 인종 객체 안의 로그인 아이디 추출함
        // 위쪽 메소드에서 인증토큰에 저장한 userId 가 인증객체 안의 name 에 저장되게 됨

//        MemberEntity member = memberRepository.findByUserid(userId)
//                .orElseThrow(() -> new RuntimeException("로그인 사용자를 찾을 수 없습니다. " + userId));
        // orElseThrow() 메소드는 findByUserid(userId) 가 Optional<MemberEntity> 반환시 사용할 수 있음
        // 내부값이 있으면 그 엔티티를 반환하고, 없으면 예외를 던지는 메소드임
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("로그인 사용자를 찾을 수 없습니다. " + userId);
        }

        // 로그인 제한 회원인지 확인
        if (user.getStatus() == 0) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"로그인 정지 회원입니다.. 관리자에게 문의하세요\"}");
            return;
        }

        // 탈퇴한 회원인지 확인 (회원 테이블에 탈퇴여부 컬럼 추가하고 사용함)

        // ✅ 로그인 성공 시 평문 비밀번호를 BCrypt로 자동 변환 (점진적 마이그레이션)
        if (user.getUserPwd() != null && !user.getUserPwd().startsWith("$2a$")) {
            String hashedPassword = passwordEncoder.encode(user.getUserPwd());
            user.setUserPwd(hashedPassword);
            userRepository.save(user);
            log.info("비밀번호 자동 마이그레이션 완료: userId={}", user.getUserId());
        }

        // 정상적인 회원이면, jwt 토큰 (압축된 문자열) 생성함
        // access token 생성
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        // refresh token 생성
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // 리프레시 토큰은 db에 저장
        // 만약, MEMBER 테이블에 refreshToken 저장 컬럼을 추가했다면
//        member.setRefreshToken(refreshToken);
//        memberService.updateRefreshToken(member);  // update 쿼리문 실행, 트랜잭션 처리가 필요함

        // 리프레시토큰 저장용 테이블을 따로 준비했다면
        refreshService.saveRefresh(new RefreshToken(UUID.randomUUID().toString(), userId, refreshToken));

        // 로그인한 사용자 정보를 포함한 JSON 응답 생성
        Map<String, Object> responseBody = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "userId", userId,
                "userName", user.getUserName(),
                "role", user.getAdminYn().equals("Y") ? "ADMIN" : "USER" // 관리자여부
                // 임의 작성
        );

        response.setContentType("application/json; charset=utf-8");
        new ObjectMapper().writeValue(response.getWriter(), responseBody);  // JSON 형식으로 응답 처리
    }

    // 로그인 실패시 처리할 내용 작성용 (요청 클라이언트에게 에러 코드와 에러 메세지 보냄)
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=utf-8");

        // 디버깅을 위한 로그 추가
        log.error("로그인 실패 - 예외 타입: {}", failed.getClass().getSimpleName());
        log.error("로그인 실패 - 예외 메시지: {}", failed.getMessage());
        log.error("로그인 실패 - 전체 예외: {}", failed);

        // 예외 클래스 메세지를 기반으로 오류 메세지 지정함
        String errorMessage;
        if (failed.getMessage().contains("Bad credentials")) {
            errorMessage = "아이디와 비밀번호를 다시 확인해 주세요.";
        } else if (failed.getMessage().contains("사용자를 찾을 수 없습니다.")) {
            errorMessage = "ID 가 없으면 사용자를 찾을 수 없습니다..";
        } else {
            errorMessage = "로그인 실패 : 알 수 없는 오류가 발생했습니다.";
        }

        response.getWriter().write(String.format("{\"error\":\"%s\"}", errorMessage));
    }
}
