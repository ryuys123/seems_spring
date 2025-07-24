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
        // ⭐️ 모든 permitAll 경로를 명시적으로 startsWith로 처리하고,
        // 특히 /seems/api/analysis/status/ 경로를 가장 먼저 확인하도록 변경
        if (url.startsWith("/seems/api/analysis/status/")) {
            return true;
        }
        if (url.startsWith("/seems/images/")) {
            return true;
        }
        if (url.startsWith("/seems/api/psychological-test/image-question")) {
            return true;
        }
        if (url.startsWith("/seems/api/psychological-test/submit-answer")) {
            return true;
        }
        if (url.startsWith("/seems/api/psychological-test/results/")) {
            return true;
        }
        if (url.startsWith("/seems/api/personality-test/questions")) {
            return true;
        }
        if (url.startsWith("/seems/api/personality-test/submit-answers")) {
            return true;
        }
        if (url.startsWith("/seems/api/personality-test/results/")) {
            return true;
        }
        if (url.startsWith("/seems/api/personality-test/history/")) {
            return true;
        }
        if (url.startsWith("/seems/api/psychological-test/")) {
            return true;
        }
        if (url.startsWith("/seems/api/quest-rewards")) {
            return true;
        }
        if (url.startsWith("/seems/api/user/points")) {
            return true;
        }
        if (url.startsWith("/seems/api/user/stats")) {
            return true;
        }
        if (url.startsWith("/seems/api/user/owned-titles")) {
            return true;
        }
        if (url.startsWith("/api/simulation/")) {
            return true;
        }
        if (url.startsWith("/seems/api/simulation/")) {
            return true;
        }
        if (url.startsWith("/seems/api/emotions")) {
            return true;
        }
        if (url.startsWith("/seems/api/face/")) {
            return true;
        }
        if (url.startsWith("/seems/auth/face")) {
            return true;
        }
        if (url.startsWith("/seems/user/signup")) {
            return true;
        }
        if (url.startsWith("/seems/user/idchk")) {
            return true;
        }
        if (url.startsWith("/seems/login")) {
            return true;
        }
        if (url.startsWith("/seems/css/")) {
            return true;
        }
        if (url.startsWith("/seems/js/")) {
            return true;
        }
        if (url.startsWith("/seems/favicon.ico")) {
            return true;
        }
        if (url.startsWith("/seems/api/face-signup")) {
            return true;
        }
        if (url.startsWith("/api/face/login")) {
            return true;
        }
        if (url.startsWith("/api/face/register")) {
            return true;
        }
        if (url.startsWith("/api/face/")) {
            return true;
        }
        if (url.startsWith("/auth/face-login")) {
            return true;
        }
        if (url.startsWith("/notice/")) {
            return true;
        }
        if (url.startsWith("/admin/")) {
            return true;
        }
        if (url.startsWith("/js/")) {
            return true;
        }
        if (url.endsWith(".png")) {
            return true;
        }
        if (url.startsWith("/notice/attachments/")) {
            return true;
        }
        if (url.startsWith("/payments/request/")) {
            return true;
        }
        if (url.equals("/")) {
            return true;
        }
        if (url.equals("/favicon.ico")) {
            return true;
        }
        if (url.equals("/manifest.json")) {
            return true;
        }
        if (url.startsWith("/public/")) {
            return true;
        }
        if (url.startsWith("/auth/")) {
            return true;
        }
        if (url.startsWith("/css/")) {
            return true;
        }
        if (url.startsWith("/api/psychological-test/")) {
            return true;
        }
        if (url.startsWith("/api/simulation/")) {
            return true;
        }
        if (url.startsWith("/api/emotions")) {
            return true;
        }
        if (url.startsWith("/logout")) {
            return false;
        }
        return false;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.info("JWTFilter - Request URI: {}", requestURI);

        if (isExcludedUrl(requestURI)) {
            log.info("Token check skipped for: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Token check target URL: {}", requestURI);

        String authorizationHeader = request.getHeader("Authorization");

        // 1. Authorization 헤더 존재 및 형식 확인
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or invalid.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
            return;
        }

        String accessToken = authorizationHeader.substring("Bearer ".length());

        // 2. Access Token 만료 여부 확인
        if (jwtUtil.isTokenExpired(accessToken)) {
            log.warn("Access Token has expired.");
            // 클라이언트가 토큰 재발급을 시도하도록 401 응답과 함께 특정 헤더나 메시지를 보낼 수 있습니다.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("token-expired", "AccessToken");
            response.getWriter().write("{\"error\":\"Access Token expired\"}");
            return;
        }

        try {
            // 3. Access Token에서 사용자 정보 추출
            String username = jwtUtil.getUseridFromToken(accessToken);
            if (username == null) {
                throw new Exception("Invalid Access Token: Username is null");
            }

            // 4. UserDetails 객체를 가져와 SecurityContext에 인증 정보 설정
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 5. 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error during JWT token validation", e);
            SecurityContextHolder.clearContext(); // 보안 컨텍스트 정리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid Token\"}");
        }
    }
}
