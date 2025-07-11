package com.test.seems.security.config;

import com.test.seems.security.filter.JWTFilter;
import com.test.seems.security.filter.LoginFilter;
import com.test.seems.security.handler.CustomLogoutHandler;
import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.security.jwt.model.service.RefreshService;
import com.test.seems.security.model.service.CustomUserDetailsService;
import com.test.seems.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    // LoginFilter 는 Spring 컨테이너가 관리하는 컴포넌트가 아님
    // 필터에서 직접 의존성 주입 못 함 (@Autowired 사용 못 함)
    // SecurityConfig 에서 의존성 주입(외부 클래스 객체 생성)해서 필터로 전달함

    private final JWTUtil jwtUtil;  // 아래의 필터 추가 부분에 LoginFilter 생성자 사용에 필요해서 준비한 것임
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;   // 아래의 필터 추가 부분에 LoginFilter 생성자 사용에 필요해서 준비한 것임
    private final RefreshService refreshService; // LoginFilter 생성자에 필요해서 준비함

    // 멤버변수에 final 사용하면, 매개변수 있는 생성자로 의존성 주입 처리해야 함
    // @RequiredArgsConstructor 로 대신해도 됨
    public SecurityConfig(JWTUtil jwtUtil, CustomUserDetailsService userDetailsService,
                          UserRepository userRepository, RefreshService refreshService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.refreshService = refreshService;
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler(RefreshService refreshService) {
        return new CustomLogoutHandler(jwtUtil, refreshService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ✅ BCryptPasswordEncoder 사용 (안전한 해싱)
        // 임시로 평문도 허용하는 하이브리드 방식 (테스트용)
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
            
            @Override
            public String encode(CharSequence rawPassword) {
                return bcryptEncoder.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // BCrypt 해싱된 비밀번호인 경우
                if (encodedPassword != null && encodedPassword.startsWith("$2a$")) {
                    return bcryptEncoder.matches(rawPassword, encodedPassword);
                }
                // 평문 비밀번호인 경우 (임시 허용)
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 (Authentication) 관리자를 스프링부트 컨테이너에 Bean 으로 등록해야 함
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    // ---------------------------------------------------------
    // CORS (Cross-Origin Resource Sharing) 문제 해결해야 함
    // 브라우저 보안 정책임 : 포트번호가 다른 도메인에서 다른 도메인의 리소스를 요청할 때, 허용되지 않게 되어 있음
    // 리액트 (3000번) 에서 오는 요청을 받도록 설정해야 함
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("token-expired", "Authorization", "RefreshToken")
                .allowCredentials(true);
    }
    // -------------------------------------------------------

    // http 관련 보안 설정을 정의하는 메소드임
    // SecurityFilterChain 을 Bean 으로 등록하고, http 서비스 요청에 대한 보안 설정을 정의함
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, CustomLogoutHandler customLogoutHandler) throws Exception {
        //http.csrf(AbstractHttpConfigurer::disable);  // 아래 코드도 동일한 기능임
        //Spring Security 6 이상에서 사용하는 람다 기반 보안 설정 방식 사용함
        http
                .csrf(csrf -> csrf.disable())
                // CSRF는 사용자가 인증된 세션을 가진 상태에서 악성 요청이 전송되는 것을 방지하는 보안 기능
                //  CSRF(Cross Site Request Forgery, 교차 사이트 요청 위조) 보호 설정을 비활성화
                // 기본적으로 Spring Security는 POST, PUT, DELETE 요청에 대해 CSRF 토큰을 요구함
                // React, Vue 등 프론트엔드가 별도로 있고, JWT 토큰을 이용한 인증 방식일 때 비활성화함
                .cors(cors -> {})  // CORS 설정 활성화
                .formLogin(form -> form.disable())  //시큐리티가 제공하는 로그인 폼 사용 못하게 함
                .httpBasic(basic -> basic.disable())  //form 태그로 submit 해서 오는 요청은 사용 못하게 함
                //인증과 인가를 설정하는 부분
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 및 인증(/auth/**) 관련 요청은 인가에서 제외하기 위한 url 지정 (무조건 통과됨)
                        .requestMatchers("/", "/**", "/favicon.ico", "/manifest.json", "/public/**", "/auth/**",
                                "/css/**", "/js/**").permitAll()
                        // .png 파일은 인증없이 접근 허용함
                        .requestMatchers("/*.png").permitAll()
                        // 로그인, 토큰 재발급, 회원가입도 인증없이 접근 허용함
                        .requestMatchers("/", "/login", "/reissue", "seems/user/signup", "seems/user/idchk", "/admin",
                                         "/api/personality-test/questions", "/api/personality-test/submit-answers",
                                        "/api/psychological-test/**").permitAll()
                        // 로그아웃은 인증된 사용자만 요청 가능 (인가 확인 필요)
                        .requestMatchers("/logout").authenticated()
                        // 관리자 전용 서비스인 경우 ROLE_ADMIN 권한 확인 필요함
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/notice/delete/*").hasAnyRole("ADMIN")  // 공지 삭제는 ADMIN 만 허용
                        // 나머지 모든 요청은 인증 확인 필요함 (로그인해야 요청할 수 있는 서비스들)
                        .anyRequest().authenticated()
                )
                // 모든 url 이 인가에서는 제외되었지만 (permitAll) JWTFilter 는 무조건 실행됨 => 토큰 검사함
                // 해결방법 : JWTFilter 안에 특정 url 에 대해 토큰검사 제외하는 기능 추가함
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                // 로그인 인증(Authentication) 은 인증 관리자(AuthenticationManager)가 관리해야 함
                .addFilterAt(new LoginFilter(authenticationManager, jwtUtil, userRepository, refreshService, passwordEncoder()),
                        UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 요청 url 지정
                        .addLogoutHandler(customLogoutHandler)  // SecurityFilterChain 에 추가할 Handler 등록 : CustomLogoutHandler 등록
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // 오버라이딩한 logout() 을 작동시키고 리턴된 성공정보를 클라이언트에게 내보냄
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("로그아웃 성공");
                        })
                        .invalidateHttpSession(true)   // 세션 무효화
                        .clearAuthentication(true)  // 인증 정보 제거
                        .deleteCookies("JSESSIONID")  // 쿠키 제거
                );

        return http.build();
    }



}
