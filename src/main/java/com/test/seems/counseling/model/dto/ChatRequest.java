package com.test.seems.counseling.model.dto;

import java.util.List;

public class ChatRequest {
    private List<QAPair> qa_pairs;

    // Getters and Setters
    public List<QAPair> getQa_pairs() {
        return qa_pairs;
    }

    public void setQa_pairs(List<QAPair> qa_pairs) {
        this.qa_pairs = qa_pairs;
    }
}
