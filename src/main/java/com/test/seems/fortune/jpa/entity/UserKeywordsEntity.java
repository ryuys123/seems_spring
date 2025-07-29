package com.test.seems.fortune.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER_KEYWORDS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKeywordsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KEYWORD_ID")
    private Long keywordId;

    @Column(name = "USER_ID", nullable = false, length = 255)
    private String userId;

    @Column(name = "GUIDANCE_TYPE_ID", nullable = false)
    private Long guidanceTypeId;

    @Column(name = "IS_SELECTED", nullable = false)
    private Boolean isSelected;

    @Column(name = "UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (updatedDate == null) {
            updatedDate = LocalDateTime.now();
        }
        if (isSelected == null) {
            isSelected = false;
        }
    }
} 