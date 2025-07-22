package com.test.seems.security.filter;

import com.test.seems.security.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService; // UserDetailsService 주입

    public JWTFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // 개발단계에서 SecurityConfig 설정은 그대로 둔 상태에서 토큰 없이 서비스 구동을 확인하려면
    // 이 필터에서 토큰 확인 안 하고 서비스로 넘기도록 하면 됨
    // 그냥 통과시킬 url 등록 메소드를 추가함
    private boolean isExcludedUrl(String url) {
        // ⭐️ 추가된 부분: /seems/images/ 디렉토리의 모든 파일에 대해 토큰 검사 없이 통과
        if (url.startsWith("/seems/images/")) {
            return true;
        }
        return url.equals("/")
                || url.equals("/favicon.ico")
                || url.startsWith("/seems/login")
                || url.equals("/seems/notice/*")
                || url.equals("/seems/faq/*")
                || url.equals("/seems/user/signup")
                || url.equals("/seems/user/idchk")
                || url.equals("/seems/api/face-signup")
                || url.startsWith("/seems/api/face/")
                || url.startsWith("/seems/auth/face")
                || url.equals("/api/face/login")
                || url.equals("/api/face/register")
                || url.equals("/api/face/signup")

                || url.startsWith("/seems/api/psychological-test/image-question") // 이미지 문항 조회
                || url.startsWith("/seems/api/psychological-test/submit-answer") // 답변 제출
                || url.startsWith("/seems/api/psychological-test/results/") // 결과 조회

                || url.startsWith("/seems/api/personality-test/questions")
                || url.startsWith("/seems/api/personality-test/submit-answers")
                || url.startsWith("/seems/api/personality-test/results/")
                || url.startsWith("/seems/api/personality-test/history/")

                || url.startsWith("/seems/api/psychological-test/")
                // 퀘스트 경로 추가
                || url.startsWith("/seems/api/quest-rewards") // 뱃지 상점 API
                || url.startsWith("/seems/api/user/points") // 사용자 포인트 조회
                || url.startsWith("/seems/api/user/stats") // 사용자 통계 조회
                || url.startsWith("/seems/api/user/owned-titles") // 사용자 보유 뱃지 조회

                // ** 시뮬레이션 API 경로 추가 **
                || url.startsWith("/api/simulation/")
                || url.startsWith("/seems/api/simulation/")

                // 감정 API 경로 추가 (별도 라인으로 분리)
                || url.startsWith("/seems/api/emotions")

                // 문자열 비교이기 때문에 실제 요청 경로가 /notice/detail/13일 경우,
                // "/notice/detail/*".equals("/notice/detail/13") → false가 됩니다.
                // 해결 방법: startsWith()로 경로 시작 여부로 검사
                || url.equals("/js/**")
                || url.endsWith(".png")
                || url.equals("/notice/attachments/")  // notice 첨부파일들 등록
                || url.equals("/payments/request/");


    }



    // 필터의 주요 로직을 구현하는 메소드임
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        // 토큰 정보 추출해서 토큰이 유효한지 확인하는 로직 구현함 
        String requestURI = request.getRequestURI();  // 요청한 서비스 url 추출
        log.info("JWTFilter 작동중 - requestURI: {}", requestURI);  // 필터 작동 확인용

        // 그냥 통과시킬 url 처리
        if (isExcludedUrl(requestURI)) {
            log.info("토큰 검사없이 통과 : " + requestURI);
            filterChain.doFilter(request, response);  // 다음 단계로 넘김
            return;
        }
        
        log.info("토큰 검사 대상 URL: " + requestURI);

        // 클라이언트 쪽에서 토큰 저장 방식을 미리 정해 놓고 맞춰야 함
        String accessTokenHeader = request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("RefreshToken");

        if (accessTokenHeader == null || accessTokenHeader.isEmpty()) {
            log.warn("Authorization header is empty");
        }
        if (refreshTokenHeader == null || refreshTokenHeader.isEmpty()) {
            log.warn("Refresh header is empty");
        }

        try {
                String accessToken = null;
                String refreshToken = null;

                if ((accessTokenHeader != null && refreshTokenHeader != null)) {
                    // 각 해더에서 token 추출
                    accessToken = accessTokenHeader.substring("Bearer ".length());
                    refreshToken = refreshTokenHeader.substring("Bearer ".length());

                    // AccessToken에서 사용자 이름 추출 및 인증 정보 설정
                    String username = jwtUtil.getUseridFromToken(accessToken);
                    
                    // 페이스 로그인 토큰인지 확인
                    String authType = jwtUtil.getAuthTypeFromToken(accessToken);
                    log.info("JWT 토큰 인증 타입: {}, 사용자: {}", authType, username);
                    
                    UserDetails userDetails = null;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (Exception e) {
                        log.warn("UserDetails 조회 실패: {}", e.getMessage());
                        // 페이스 로그인 또는 일반 로그인의 경우 간단한 인증 객체 생성
                        if ("FACE".equals(authType) || authType == null) {
                            userDetails = org.springframework.security.core.userdetails.User.builder()
                                    .username(username)
                                    .password("") // 페이스 로그인은 비밀번호 검증 불필요
                                    .roles("USER") // 기본 역할
                                    .build();
                        }
                    }
                    
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    } else {
                        log.error("UserDetails가 null입니다. 사용자: {}", username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"invalid user\"}");
                        return;
                    }

                // RefreshToken 만료, AccessToken 유효
                if (!jwtUtil.isTokenExpired(accessToken) && jwtUtil.isTokenExpired(refreshToken)) {
                    log.warn("RefreshToken 만료, AccessToken 유효.");
                    // 요청 에러에 대한 스트림 열어서 에러 정보를 클라이언트에게 보냄
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("token-expired", "RefreshToken");
                    response.getWriter().write("{\"error\":\"RefreshToken expired\"}");
                    return;
                }

                // AccessToken 만료, RefreshToken 유효 (위의 조건 통과했으므로, 질문 생략해도 됨)
                if (jwtUtil.isTokenExpired(accessToken) && !jwtUtil.isTokenExpired(refreshToken)) {
                    log.warn("AccessToken 만료, RefreshToken 유효 - ReIssueController로 전달");
                    // AccessToken이 만료되어도 RefreshToken이 유효하면 요청을 통과시킴
                    // ReIssueController에서 토큰 갱신 처리
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // AccessToken 만료, RefreshToken도 만료
                if (jwtUtil.isTokenExpired(accessToken) && jwtUtil.isTokenExpired(refreshToken)) {
                    log.warn("AccessToken 만료, RefreshToken도 만료.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("token-expired", "Both");
                    response.getWriter().write("{\"error\":\"Both tokens expired\"}");
                    return;
                }
            } else {
                // 둘 다 null 이면
                log.warn("RefreshToken Null, AccessToken Null.");
                // 요청 에러에 대한 스트림 열어서 에러 정보를 클라이언트에게 보냄
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"missing or invalid tokens\"}");
                return;
            }

            // 두 토큰이 정상이면 로그인 상태이므로 다음 단계로 넘김
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWTFilter 에서 토큰 검사 중 에러 발생함", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"internal server error\"}");
        }
        
    }
}
