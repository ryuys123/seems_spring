package com.test.seems.faq.controller;

import com.test.seems.faq.model.dto.Faq;
import com.test.seems.faq.model.dto.Reply;
import com.test.seems.faq.model.service.FaqService;
import com.test.seems.faq.model.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j   // log 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@RequiredArgsConstructor
@RestController
@CrossOrigin   //다른 url port 에서 오는 요청을 처리하기 위함 (리액트 port 3000번, 리액트에서 부트(8080)로 요청함)
public class ReplyController {

    private final ReplyService ReplyService;

    @GetMapping("/faq/detail/{faqNo}/replies")
    public ResponseEntity<ArrayList<Reply>> getReplies(@PathVariable int faqNo, Principal principal) {
        try {
            System.out.println("👉 댓글 불러오기 요청: faqNo = " + faqNo);
            ArrayList<Reply> replies = ReplyService.getRepliesByFaqNo(faqNo);
            System.out.println("✅ 불러온 댓글 수: " + replies.size());
            for (Reply r : replies) {
                System.out.println("댓글 내용: " + r.getContent());
            }
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }
}
