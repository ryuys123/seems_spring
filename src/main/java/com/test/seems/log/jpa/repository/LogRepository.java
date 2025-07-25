package com.test.seems.log.jpa.repository;

import com.test.seems.log.jpa.entity.LogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {

    int countByActionContainingIgnoreCase(String action);
    int countBySeverityContainingIgnoreCase(String severity);
    int countByCreatedAtBetween(LocalDateTime begin, LocalDateTime  end);

    List<LogEntity> findByActionContainingIgnoreCase(String action, Pageable pageable);
    List<LogEntity> findBySeverityContainingIgnoreCase(String severity, Pageable pageable);
    List<LogEntity> findByCreatedAtBetween(LocalDateTime  start, LocalDateTime  end, Pageable pageable);
}
