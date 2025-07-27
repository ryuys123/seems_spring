package com.test.seems.counseling.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCounselingAnalysisSummaryEntity is a Querydsl query type for CounselingAnalysisSummaryEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCounselingAnalysisSummaryEntity extends EntityPathBase<CounselingAnalysisSummaryEntity> {

    private static final long serialVersionUID = 51748069L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCounselingAnalysisSummaryEntity counselingAnalysisSummaryEntity = new QCounselingAnalysisSummaryEntity("counselingAnalysisSummaryEntity");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final com.test.seems.guidance.jpa.entity.QGuidanceTypeEntity guidanceType;

    public final QCounselingSessionEntity session;

    public final StringPath summaryContent = createString("summaryContent");

    public final NumberPath<Long> summaryId = createNumber("summaryId", Long.class);

    public final StringPath summaryType = createString("summaryType");

    public QCounselingAnalysisSummaryEntity(String variable) {
        this(CounselingAnalysisSummaryEntity.class, forVariable(variable), INITS);
    }

    public QCounselingAnalysisSummaryEntity(Path<? extends CounselingAnalysisSummaryEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCounselingAnalysisSummaryEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCounselingAnalysisSummaryEntity(PathMetadata metadata, PathInits inits) {
        this(CounselingAnalysisSummaryEntity.class, metadata, inits);
    }

    public QCounselingAnalysisSummaryEntity(Class<? extends CounselingAnalysisSummaryEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.guidanceType = inits.isInitialized("guidanceType") ? new com.test.seems.guidance.jpa.entity.QGuidanceTypeEntity(forProperty("guidanceType")) : null;
        this.session = inits.isInitialized("session") ? new QCounselingSessionEntity(forProperty("session"), inits.get("session")) : null;
    }

}

