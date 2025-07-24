package com.test.seems.content.controller;

import com.test.seems.content.jpa.entity.ContentEntity;
import com.test.seems.content.model.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ContentController {
    @Autowired
    private ContentService contentService;

    // 감정ID로 추천 유튜브 컨텐츠 리스트 반환
    @GetMapping("/content-recommendations/{emotionId}")
    public ResponseEntity<List<ContentEntity>> getRecommendedContents(@PathVariable Long emotionId) {
        List<ContentEntity> contents = contentService.getRecommendedContentsByEmotionId(emotionId);
        return ResponseEntity.ok(contents);
    }
}
