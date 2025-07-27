package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPsychologicalScaleResult is a Querydsl query type for PsychologicalScaleResult
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPsychologicalScaleResult extends EntityPathBase<PsychologicalScaleResult> {

    private static final long serialVersionUID = 85972914L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPsychologicalScaleResult psychologicalScaleResult = new QPsychologicalScaleResult("psychologicalScaleResult");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath interpretation = createString("interpretation");

    public final NumberPath<Long> resultId = createNumber("resultId", Long.class);

    public final StringPath riskLevel = createString("riskLevel");

    public final StringPath suggestions = createString("suggestions");

    public final StringPath testCategory = createString("testCategory");

    public final StringPath testType = createString("testType");

    public final NumberPath<Double> totalScore = createNumber("totalScore", Double.class);

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public QPsychologicalScaleResult(String variable) {
        this(PsychologicalScaleResult.class, forVariable(variable), INITS);
    }

    public QPsychologicalScaleResult(Path<? extends PsychologicalScaleResult> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPsychologicalScaleResult(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPsychologicalScaleResult(PathMetadata metadata, PathInits inits) {
        this(PsychologicalScaleResult.class, metadata, inits);
    }

    public QPsychologicalScaleResult(Class<? extends PsychologicalScaleResult> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

