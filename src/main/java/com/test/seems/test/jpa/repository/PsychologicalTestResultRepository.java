package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PsychologicalTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalTestResultRepository extends JpaRepository<PsychologicalTestResultEntity, Long> {
    List<PsychologicalTestResultEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<PsychologicalTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
}