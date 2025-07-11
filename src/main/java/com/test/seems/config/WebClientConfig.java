package com.test.seems.config; // 프로젝트의 config 패키지 경로에 맞게 설정

import org.springframework.beans.factory.annotation.Value; // application.properties에서 값 로드
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // Spring 설정 클래스임을 명시
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient; // WebClient 임포트

@Configuration // <<-- 이 어노테이션이 반드시 있어야 Spring이 이 클래스를 설정으로 인식합니다.
public class WebClientConfig {

    // application.properties의 python.ai.server.url 값을 주입받습니다.
    @Value("${python.ai.server.url}")
    private String pythonAiServerUrl;

    /**
     * Python AI 서버와 통신할 WebClient Bean을 생성합니다.
     * Spring이 이 메소드를 호출하여 WebClient 객체를 만들고 관리합니다.
     * @param builder WebClient.Builder (Spring이 자동으로 제공)
     * @return 설정된 WebClient 인스턴스
     */
    @Bean // <<-- 이 어노테이션이 있어야 이 메소드의 반환 객체가 Spring Bean으로 등록됩니다.
    public WebClient pythonAiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(pythonAiServerUrl) // Python AI 서버의 기본 URL 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // 기본적으로 JSON 타입으로 요청
                .build();
    }
}