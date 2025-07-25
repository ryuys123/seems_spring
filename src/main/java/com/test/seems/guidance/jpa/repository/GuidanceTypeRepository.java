package com.test.seems.guidance.jpa.repository;

import com.test.seems.guidance.jpa.entity.GuidanceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuidanceTypeRepository extends JpaRepository<GuidanceTypeEntity, Long> {
    Optional<GuidanceTypeEntity> findByGuidanceTypeName(String guidanceTypeName);
}
