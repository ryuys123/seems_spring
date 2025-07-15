
package com.test.seems.counseling.jpa.repository;

import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounselingMessageRepository extends JpaRepository<CounselingMessageEntity, Long> {
    List<CounselingMessageEntity> findBySessionOrderByMessageIdAsc(CounselingSessionEntity session);
}
