package com.test.seems.counseling.controller;

import com.test.seems.counseling.model.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    @Value("${python.ai.server.url}")
    private String pythonAiServerUrl;

    private final RestTemplate restTemplate;

    public ChatController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<Map<String, Object>> chatWithAI(@RequestBody ChatRequest chatRequest) {
        List<Map<String, String>> messages = chatRequest.getMessages();
        Integer currentCoreQuestionIndex = chatRequest.getCurrent_core_question_index();

        if (messages == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Messages cannot be null");
        }
        // currentCoreQuestionIndex는 0일 수 있으므로 null 체크만 합니다.
        if (currentCoreQuestionIndex == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current_core_question_index cannot be null");
        }

        try {
            // 파이썬 AI 서버로 요청 보낼 데이터
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("messages", messages);
            pythonRequest.put("current_core_question_index", currentCoreQuestionIndex);

            // 파이썬 AI 서버 호출
            ResponseEntity<Map> pythonResponse = restTemplate.postForEntity(
                    pythonAiServerUrl + "/chat",
                    pythonRequest,
                    Map.class
            );

            if (pythonResponse.getStatusCode().is2xxSuccessful() && pythonResponse.getBody() != null) {
                String aiResponse = (String) pythonResponse.getBody().get("response");
                Integer nextCoreQuestionIndex = (Integer) pythonResponse.getBody().get("next_core_question_index");

                Map<String, Object> response = new HashMap<>();
                response.put("response", aiResponse);
                response.put("next_core_question_index", nextCoreQuestionIndex);
                return ResponseEntity.ok(response);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from Python AI server");
            }

        } catch (Exception e) {
            System.err.println("Error communicating with Python AI server: " + e.getMessage());

            // Python AI 서버가 실행되지 않은 경우 임시 응답
            if (e.getMessage().contains("404") || e.getMessage().contains("Connection refused")) {
                Map<String, Object> response = new HashMap<>();
                response.put("response", "죄송합니다. AI 상담 서버가 현재 점검 중입니다. 잠시 후 다시 시도해주세요.");
                response.put("next_core_question_index", currentCoreQuestionIndex);
                response.put("server_status", "maintenance");
                return ResponseEntity.ok(response);
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing AI request", e);
        }
    }
}