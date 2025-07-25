package com.test.seems.ai.service;

import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import java.util.List;
import java.util.Map;

public interface AIService {
    Map<String, Object> summarizeCounseling(List<CounselingMessageEntity> messages);
}
