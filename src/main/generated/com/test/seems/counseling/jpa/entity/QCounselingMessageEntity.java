package com.test.seems.counseling.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCounselingMessageEntity is a Querydsl query type for CounselingMessageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCounselingMessageEntity extends EntityPathBase<CounselingMessageEntity> {

    private static final long serialVersionUID = 1230085634L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCounselingMessageEntity counselingMessageEntity = new QCounselingMessageEntity("counselingMessageEntity");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final StringPath imageFilePath = createString("imageFilePath");

    public final StringPath messageContent = createString("messageContent");

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final DateTimePath<java.util.Date> messageTime = createDateTime("messageTime", java.util.Date.class);

    public final StringPath messageType = createString("messageType");

    public final StringPath sender = createString("sender");

    public final QCounselingSessionEntity session;

    public QCounselingMessageEntity(String variable) {
        this(CounselingMessageEntity.class, forVariable(variable), INITS);
    }

    public QCounselingMessageEntity(Path<? extends CounselingMessageEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCounselingMessageEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCounselingMessageEntity(PathMetadata metadata, PathInits inits) {
        this(CounselingMessageEntity.class, metadata, inits);
    }

    public QCounselingMessageEntity(Class<? extends CounselingMessageEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.session = inits.isInitialized("session") ? new QCounselingSessionEntity(forProperty("session"), inits.get("session")) : null;
    }

}

