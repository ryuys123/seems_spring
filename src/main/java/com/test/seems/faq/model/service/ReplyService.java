package com.test.seems.faq.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReplyService {
    
    // 기본 생성자
    public ReplyService() {
    }
    
    // 기본 메서드 추가 (나중에 실제 구현으로 교체 가능)
    public String getServiceName() {
        return "ReplyService";
    }
}
