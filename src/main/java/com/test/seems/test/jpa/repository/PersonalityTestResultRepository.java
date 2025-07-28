package com.test.seems.test.jpa.repository;

import com.test.seems.test.jpa.entity.PersonalityTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalityTestResultRepository extends JpaRepository<PersonalityTestResultEntity, Long> {
    Optional<PersonalityTestResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByUserId(String userId);
}