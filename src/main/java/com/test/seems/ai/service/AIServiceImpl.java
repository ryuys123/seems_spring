package com.test.seems.ai.service;

import com.test.seems.counseling.jpa.entity.CounselingMessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final WebClient webClient;

    @Value("${python.api.url}")
    private String aiServerUrl;

    @Override
    public Map<String, Object> summarizeCounseling(List<CounselingMessageEntity> messages) {
        log.info("Calling AI server for counseling summary...");

        List<Map<String, String>> messageDtos = messages.stream()
                .map(msg -> Map.of(
                        "sender", msg.getSender(),
                        "messageContent", msg.getMessageContent()
                ))
                .collect(Collectors.toList());

        Map<String, List<Map<String, String>>> requestBody = Map.of("messages", messageDtos);

        try {
            Mono<Map> responseMono = webClient.post()
                    .uri(aiServerUrl + "/summarize-counseling")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class);

            Map<String, Object> result = responseMono.block(); // 동기적으로 호출 (블로킹)
            log.info("AI server response: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error calling AI server for counseling summary: {}", e.getMessage());
            return Map.of("error", "Failed to get summary from AI server: " + e.getMessage());
        }
    }
}
