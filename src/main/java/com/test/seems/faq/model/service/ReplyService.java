package com.test.seems.faq.model.service;

import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.faq.jpa.entity.ReplyEntity;
import com.test.seems.faq.jpa.repository.ReplyRepository;
import com.test.seems.faq.model.dto.Faq;
import com.test.seems.faq.model.dto.Reply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j   // Logger 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@Service
@RequiredArgsConstructor
@Transactional
public class ReplyService {

    // jpa 가 제공하는 기본 메소드를 사용하려면
    @Autowired
    private final ReplyRepository replyRepository;

    // ArrayList<Reply> 리턴하는 메소드들이 사용하는 중복 코드는 별도의 메소드로 작성함
    private ArrayList<Reply> toList(List<ReplyEntity> entityList) {
        ArrayList<Reply> list = new ArrayList<>();
        for (ReplyEntity entity : entityList) {
            list.add(entity.toDto());
        }
        return list;
    }
//
//    public ArrayList<Reply> getRepliesByFaqNo(int faqNo) {
//        return toList(replyRepository.findByFaqNo(faqNo));
//    }

    public ArrayList<Reply> getRepliesByFaqNo(int faqNo) {
        List<ReplyEntity> entities = replyRepository.findByFaqNoOrderByReplyNoAsc(faqNo);
        ArrayList<Reply> list = new ArrayList<>();
        for (ReplyEntity entity : entities) {
            list.add(entity.toDto());
        }
        return list;
    }

    public Reply selectLast() {
        ReplyEntity entity = replyRepository.findTopByOrderByReplyNoDesc().orElse(null);
        return entity != null ? entity.toDto() : null;
    }

    public int insertReply(Reply reply) {
        // DTO → Entity 변환 후 save
        ReplyEntity savedEntity = replyRepository.save(reply.toEntity());
        return savedEntity != null ? 1 : 0;
    }

    public int deleteReply(int replyNo) {
        try {
            replyRepository.deleteById(replyNo);
            return 1;
        } catch (Exception e) {
            log.error("❌ 댓글 삭제 실패: " + e.getMessage());
            return 0;
        }
    }
}
