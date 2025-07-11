package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPsychologicalTestResultEntity is a Querydsl query type for PsychologicalTestResultEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPsychologicalTestResultEntity extends EntityPathBase<PsychologicalTestResultEntity> {

    private static final long serialVersionUID = 143996231L;

    public static final QPsychologicalTestResultEntity psychologicalTestResultEntity = new QPsychologicalTestResultEntity("psychologicalTestResultEntity");

    public final NumberPath<Double> aiCreativityScore = createNumber("aiCreativityScore", Double.class);

    public final StringPath aiInsightSummary = createString("aiInsightSummary");

    public final StringPath aiPerspectiveKeywords = createString("aiPerspectiveKeywords");

    public final StringPath aiSentiment = createString("aiSentiment");

    public final NumberPath<Double> aiSentimentScore = createNumber("aiSentimentScore", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath rawResponseText = createString("rawResponseText");

    public final NumberPath<Long> resultId = createNumber("resultId", Long.class);

    public final StringPath suggestions = createString("suggestions");

    public final StringPath userId = createString("userId");

    public QPsychologicalTestResultEntity(String variable) {
        super(PsychologicalTestResultEntity.class, forVariable(variable));
    }

    public QPsychologicalTestResultEntity(Path<? extends PsychologicalTestResultEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPsychologicalTestResultEntity(PathMetadata metadata) {
        super(PsychologicalTestResultEntity.class, metadata);
    }

}

