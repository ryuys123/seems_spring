package com.test.seems.counseling.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCounselingSessionEntity is a Querydsl query type for CounselingSessionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCounselingSessionEntity extends EntityPathBase<CounselingSessionEntity> {

    private static final long serialVersionUID = -1649886895L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCounselingSessionEntity counselingSessionEntity = new QCounselingSessionEntity("counselingSessionEntity");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final DateTimePath<java.util.Date> endTime = createDateTime("endTime", java.util.Date.class);

    public final StringPath method = createString("method");

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public final DateTimePath<java.util.Date> startTime = createDateTime("startTime", java.util.Date.class);

    public final StringPath topic = createString("topic");

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public QCounselingSessionEntity(String variable) {
        this(CounselingSessionEntity.class, forVariable(variable), INITS);
    }

    public QCounselingSessionEntity(Path<? extends CounselingSessionEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCounselingSessionEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCounselingSessionEntity(PathMetadata metadata, PathInits inits) {
        this(CounselingSessionEntity.class, metadata, inits);
    }

    public QCounselingSessionEntity(Class<? extends CounselingSessionEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

