package com.test.seems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}, scanBasePackages = "com.test.seems")
@SpringBootApplication
public class FirstApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirstApplication.class, args);

		// HS512 (512bit == 64byte) 알고리즘 적용 시크릿키 생성 코드임
		// 출력된 비밀키를 application.properties 안의 jwt.secrit=복사해넣음
//		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//		System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
	}

}
