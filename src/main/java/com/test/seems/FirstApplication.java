package com.test.seems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}, scanBasePackages = "com.test.seems")
@SpringBootApplication
public class FirstApplication {

	public static void main(String[] args) {
		// 비밀번호 해싱 테스트
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String testPassword = "123456";
		String hashedPassword = encoder.encode(testPassword);

		System.out.println("=== 비밀번호 해싱 테스트 ===");
		System.out.println("평문 비밀번호: " + testPassword);
		System.out.println("해싱된 비밀번호: " + hashedPassword);

		// 비밀번호 검증 테스트
		boolean isValid = encoder.matches(testPassword, hashedPassword);
		System.out.println("비밀번호 검증 결과: " + isValid);

		// 기존 DB 값으로 테스트
		String oldHashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
		boolean isOldValid = encoder.matches(testPassword, oldHashedPassword);
		System.out.println("기존 DB 값 검증 결과: " + isOldValid);

		System.out.println("\n=== DB 업데이트용 SQL ===");
		System.out.println("UPDATE TB_USERS SET USER_PWD = '" + hashedPassword + "' WHERE USER_ID = 'user002';");
		System.out.println("================================\n");

		SpringApplication.run(FirstApplication.class, args);

		// HS512 (512bit == 64byte) 알고리즘 적용 시크릿키 생성 코드임
		// 출력된 비밀키를 application.properties 안의 jwt.secrit=복사해넣음
//      Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//      System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}


}
