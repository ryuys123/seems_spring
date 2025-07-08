package com.test.seems.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.security.jwt.model.service.RefreshService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * SecurityFileterChain 의 LogoutFileter 가 LogoutHanlder (LogoutSuccessHandler 의 부모임)를 작동시키도록 기본 구조가 되어있음
 * 이 프로젝트에서 요구되는 로그아웃 처리 코드를 별도로 작성해서,
 * LogoutFilter 에 의해 자동 실행되게 하려면 LogoutHandler 를 상속받은 후손클래스 만들어서 메소드 오버라이딩시에
 * 원하는 코드 작성해 넣으면 됨
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    /**
     * 로그인시 발급된 토큰을 제거하는 처리를 해야함
     * 요청객체 (request) 에서 Header 에 기록된 인가(Authorization) 정보에서 토큰을 분리 추출함
     * 토큰(json 구조)에서 userid 정보를 추출함 => userid 를 db로 보내서 RefreshToken 값을 조회해 옴
     * 토큰 존재 확인하고 해당 토큰을 db에서 삭제 처리함
     * 클라이언트에게 로그아웃 요청 성공 코드를 보냄 => response 를 통해 응답 처리함
     * 오류 발생시에는 에러에 대한 http status code 를 선택해서 response 를 통해 응답 처리함
     * */

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("로그아웃 커스텀 클래스의 logout() 메소드 실행됨....");

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            // accessToken 추출함
            String accessToken = authorization.substring("Bearer ".length()).trim();

            try {
                String userId = jwtUtil.getUseridFromToken(accessToken);
                if (userId != null) {
                    // userId 로 refreshToken 조회해 옴
                    String refreshToken = refreshService.selectTokenValue(userId);

                    // userId 와 refreshToken 으로 id 조회해서 db 정보 삭제 처리
                    refreshService.deleteRefresh(refreshService.selectId(userId, refreshToken));

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("로그아웃 성공");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try {
                    response.getWriter().write("로그아웃 처리 중 오류 발생");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("유효하지 않은 요청");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
