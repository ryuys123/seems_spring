package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScaleAnalysisResultEntity is a Querydsl query type for ScaleAnalysisResultEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScaleAnalysisResultEntity extends EntityPathBase<ScaleAnalysisResultEntity> {

    private static final long serialVersionUID = 1393051494L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScaleAnalysisResultEntity scaleAnalysisResultEntity = new QScaleAnalysisResultEntity("scaleAnalysisResultEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath interpretation = createString("interpretation");

    public final NumberPath<Long> resultId = createNumber("resultId", Long.class);

    public final StringPath riskLevel = createString("riskLevel");

    public final StringPath suggestions = createString("suggestions");

    public final StringPath testCategory = createString("testCategory");

    public final StringPath testType = createString("testType");

    public final NumberPath<Double> totalScore = createNumber("totalScore", Double.class);

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public QScaleAnalysisResultEntity(String variable) {
        this(ScaleAnalysisResultEntity.class, forVariable(variable), INITS);
    }

    public QScaleAnalysisResultEntity(Path<? extends ScaleAnalysisResultEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScaleAnalysisResultEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScaleAnalysisResultEntity(PathMetadata metadata, PathInits inits) {
        this(ScaleAnalysisResultEntity.class, metadata, inits);
    }

    public QScaleAnalysisResultEntity(Class<? extends ScaleAnalysisResultEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

