package com.test.seems.analysis.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserAnalysisSummaryEntity is a Querydsl query type for UserAnalysisSummaryEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAnalysisSummaryEntity extends EntityPathBase<UserAnalysisSummaryEntity> {

    private static final long serialVersionUID = 869599288L;

    public static final QUserAnalysisSummaryEntity userAnalysisSummaryEntity = new QUserAnalysisSummaryEntity("userAnalysisSummaryEntity");

    public final StringPath analysisComment = createString("analysisComment");

    public final NumberPath<Integer> analysisCompleted = createNumber("analysisCompleted", Integer.class);

    public final NumberPath<Long> counselingSummaryId = createNumber("counselingSummaryId", Long.class);

    public final StringPath dominantEmotion = createString("dominantEmotion");

    public final NumberPath<Long> emotionId = createNumber("emotionId", Long.class);

    public final StringPath individualResultsJson = createString("individualResultsJson");

    public final DatePath<java.util.Date> lastUpdated = createDate("lastUpdated", java.util.Date.class);

    public final NumberPath<Long> personalityResultId = createNumber("personalityResultId", Long.class);

    public final NumberPath<Long> psychoImageResultId = createNumber("psychoImageResultId", Long.class);

    public final NumberPath<Long> psychoScaleResultId = createNumber("psychoScaleResultId", Long.class);

    public final NumberPath<Long> simulationResultId = createNumber("simulationResultId", Long.class);

    public final StringPath userId = createString("userId");

    public final NumberPath<Long> userSummaryId = createNumber("userSummaryId", Long.class);

    public QUserAnalysisSummaryEntity(String variable) {
        super(UserAnalysisSummaryEntity.class, forVariable(variable));
    }

    public QUserAnalysisSummaryEntity(Path<? extends UserAnalysisSummaryEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserAnalysisSummaryEntity(PathMetadata metadata) {
        super(UserAnalysisSummaryEntity.class, metadata);
    }

}

