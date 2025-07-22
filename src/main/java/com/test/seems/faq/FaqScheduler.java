package com.test.seems.faq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.test.seems.faq.model.service.FaqService;

@Slf4j
@Component
@EnableScheduling  // ✅ 여기에 붙이면 글로벌 설정 안 건드려도 됨!
@RequiredArgsConstructor
public class FaqScheduler {

    private final FaqService faqService;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void autoCloseFaqs() {
        log.info("🕛 자정 스케줄러 실행됨 - 상담 자동 종료 체크");
        faqService.autoCloseFaqs();  // 서비스에서 처리
    }
}