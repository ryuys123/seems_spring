package com.test.seems.emotion.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmotionLog is a Querydsl query type for EmotionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionLog extends EntityPathBase<EmotionLog> {

    private static final long serialVersionUID = 454054858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmotionLog emotionLog = new QEmotionLog("emotionLog");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final QEmotion emotion;

    public final NumberPath<Long> emotionLogId = createNumber("emotionLogId", Long.class);

    public final StringPath textContent = createString("textContent");

    public final DateTimePath<java.util.Date> updatedAt = createDateTime("updatedAt", java.util.Date.class);

    public final StringPath userId = createString("userId");

    public QEmotionLog(String variable) {
        this(EmotionLog.class, forVariable(variable), INITS);
    }

    public QEmotionLog(Path<? extends EmotionLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmotionLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmotionLog(PathMetadata metadata, PathInits inits) {
        this(EmotionLog.class, metadata, inits);
    }

    public QEmotionLog(Class<? extends EmotionLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.emotion = inits.isInitialized("emotion") ? new QEmotion(forProperty("emotion")) : null;
    }

}

