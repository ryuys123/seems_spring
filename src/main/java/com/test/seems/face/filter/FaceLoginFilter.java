package com.test.seems.face.filter;

import com.test.seems.security.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
public class FaceLoginFilter extends OncePerRequestFilter {
    
    private final JWTUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ 추가된 부분: OPTIONS 메서드 요청은 토큰/페이스 로그인 검사 없이 바로 통과
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK); // CORS 사전 검증 성공 응답 (200 OK)
            log.info("FaceLoginFilter: OPTIONS 메서드 요청 통과 - URL: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return; // 여기서 필터 체인 종료
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // 페이스 로그인 토큰 검증
            String category = jwtUtil.getCategory(token);
            String authType = jwtUtil.getClaimsFromToken(token).get("authType", String.class);
            
            // 페이스 로그인 토큰인 경우 (category가 access/refresh이고 authType이 FACE인 경우)
            if (("access".equals(category) || "refresh".equals(category)) && "FACE".equals(authType)) {
                String userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                
                if (userId != null && username != null) {
                    // 인증 객체 생성 (userId를 principal로 세팅)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, null, new ArrayList<>()
                    );
                    
                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("페이스 로그인 토큰 검증 성공: 사용자 {} (category: {}, authType: {})", userId, category, authType);
                } else {
                    log.warn("페이스 로그인 토큰에서 사용자 정보 추출 실패: userId={}, username={}", userId, username);
                }
            }
            
        } catch (Exception e) {
            log.warn("페이스 로그인 토큰 검증 실패: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
} 