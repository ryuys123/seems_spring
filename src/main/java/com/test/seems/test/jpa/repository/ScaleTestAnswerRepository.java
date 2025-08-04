package com.test.seems.test.jpa.repository;

import com.test.seems.test.model.entity.ScaleTestAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScaleTestAnswerRepository extends JpaRepository<ScaleTestAnswerEntity, Long> {

}