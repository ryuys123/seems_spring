package com.test.seems.counseling.model.dto;

import java.util.List;
import java.util.Map;

public class ChatRequest {
    private List<Map<String, String>> messages;
    private Integer current_core_question_index;

    // Getters and Setters
    public List<Map<String, String>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, String>> messages) {
        this.messages = messages;
    }

    public Integer getCurrent_core_question_index() {
        return current_core_question_index;
    }

    public void setCurrent_core_question_index(Integer current_core_question_index) {
        this.current_core_question_index = current_core_question_index;
    }
}
