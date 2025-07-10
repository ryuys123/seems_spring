package com.test.seems.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.security.jwt.JWTUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
// Spring security 가 제공하는 OncePerRequestFilter 를 상속받음
// => 모든 인증검사에 해당하는 요청 (SecurityConfig 에 authenticated() 지정된 url)에 대해 한번씩 실행되는 필터가 됨
// 토큰 검사하는 필터로 준비함
// 컨트롤러 메소드 서비스로 전달되기 전에 토큰이 유효한지 검사하는 필터로 사용함
public class JWTFilter extends OncePerRequestFilter {
    
    // 웹 서비스 요청 각각에 대해 로그인 상태 토큰을 취급하기 위해 JWTUtil 의존성 주입함
    private final JWTUtil jwtUtil;  // 어노테이션사용 또는 생성자 직접 작성

    // 생성자 직접 작성 => @RequiredArgsConstructor 를 사용해도 됨
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 개발단계에서 SecurityConfig 설정은 그대로 둔 상태에서 토큰 없이 서비스 구동을 확인하려면
    // 이 필터에서 토큰 확인 안 하고 서비스로 넘기도록 하면 됨
    // 그냥 통과시킬 url 등록 메소드를 추가함
    private boolean isExcludedUrl(String url) {
        return url.equals("/")
                || url.equals("/favicon.ico")
                || url.startsWith("/seems/login")
                || url.equals("/seems/notice/*")
                || url.equals("/seems/faq/*")
                || url.equals("/seems/user/signup")
                || url.equals("/seems/user/idchk")
                || url.startsWith("/seems/api/personality-test/questions")
                || url.startsWith("/seems/api/personality-test/submit-answers")
                || url.startsWith("/seems/api/psychological-test/")
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
            if ((accessTokenHeader != null && refreshTokenHeader != null)) {
                // 각 해더에서 token 추출
                String accessToken = accessTokenHeader.substring("Bearer ".length());
                String refreshToken = refreshTokenHeader.substring("Bearer ".length());

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
                if (jwtUtil.isTokenExpired(accessToken)) {
                    log.warn("RefreshToken 유효, AccessToken 만료.");
                    // 요청 에러에 대한 스트림 열어서 에러 정보를 클라이언트에게 보냄
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("token-expired", "AccessToken");
                    response.getWriter().write("{\"error\":\"AccessToken expired\"}");
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
