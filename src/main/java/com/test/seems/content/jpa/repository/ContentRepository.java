package com.test.seems.content.jpa.repository;

import com.test.seems.content.jpa.entity.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<ContentEntity, Long> {
    ContentEntity findByYoutubeId(String youtubeId);
}
