package com.test.seems.faq.jpa.repository;

import com.test.seems.faq.jpa.entity.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<ReplyEntity, Integer> {
//    List<ReplyEntity> findByFaqNo(int faqNo);

    @Query("SELECT r FROM ReplyEntity r WHERE r.faqNo = :faqNo")
    List<ReplyEntity> findRepliesByFaqNo(@Param("faqNo") int faqNo);

}