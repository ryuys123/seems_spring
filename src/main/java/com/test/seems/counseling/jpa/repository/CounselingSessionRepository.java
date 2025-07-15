
package com.test.seems.counseling.jpa.repository;

import com.test.seems.counseling.jpa.entity.CounselingSessionEntity;
import com.test.seems.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSessionEntity, Long> {
    List<CounselingSessionEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
}
