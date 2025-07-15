package com.test.seems.faq.jpa.repository;

import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.notice.jpa.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Integer> {

    // 최근 faq글 1개 조회 (가장 큰 faq글 번호 1개 조회)
    Optional<FaqEntity> findTopByOrderByFaqNoDesc();
}
