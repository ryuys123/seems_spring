package com.test.seems.test.jpa.entity;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "TB_PSYCHOLOGICAL_SCALE_RESULTS")
@Data
public class ScaleAnalysisResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESULT_ID")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, referencedColumnName = "USER_ID")
    private UserEntity user;

    @Column(name = "TEST_CATEGORY", nullable = false)
    private String testCategory;

    @Column(name = "TOTAL_SCORE", nullable = false)
    private Double totalScore;

    @Lob
    @Column(name = "INTERPRETATION")
    private String interpretation;

    @Column(name = "RISK_LEVEL")
    private String riskLevel;

    @Lob
    @Column(name = "SUGGESTIONS")
    private String suggestions;

    @Column(name = "CREATED_AT", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private Timestamp createdAt;

    // toDto 메서드는 필요에 따라 추가
}
