package com.test.seems.counseling.jpa.entity;

import com.test.seems.guidance.jpa.entity.GuidanceTypeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "TB_COUNSELING_ANALYSIS_SUMMARIES")
public class CounselingAnalysisSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "counseling_analysis_summary_seq")
    @SequenceGenerator(name = "counseling_analysis_summary_seq", sequenceName = "SEQ_COUNSELING_ANALYSIS_SUMMARIES_SUMMARY_ID", allocationSize = 1)
    @Column(name = "SUMMARY_ID")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SESSION_ID", nullable = false)
    private CounselingSessionEntity session;

    @Column(name = "SUMMARY_TYPE", nullable = false, length = 20)
    private String summaryType; // TEXT, VOICE

    @ManyToOne(fetch = FetchType.LAZY) // 추가
    @JoinColumn(name = "GUIDANCE_TYPE_ID", nullable = false) // 추가
    private GuidanceTypeEntity guidanceType; // FK to TB_GUIDANCE_TYPES (변경)

    @Column(name = "SUMMARY_CONTENT", nullable = false, length = 2000)
    private String summaryContent;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private Date createdAt;

    @Builder
    public CounselingAnalysisSummaryEntity(CounselingSessionEntity session, String summaryType, GuidanceTypeEntity guidanceType, String summaryContent) { // 변경
        this.session = session;
        this.summaryType = summaryType;
        this.guidanceType = guidanceType; // 변경
        this.summaryContent = summaryContent;
    }
}