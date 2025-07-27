package com.test.seems.emotion.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmotion is a Querydsl query type for Emotion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotion extends EntityPathBase<Emotion> {

    private static final long serialVersionUID = 815008026L;

    public static final QEmotion emotion = new QEmotion("emotion");

    public final StringPath description = createString("description");

    public final NumberPath<Long> emotionId = createNumber("emotionId", Long.class);

    public final StringPath emotionName = createString("emotionName");

    public QEmotion(String variable) {
        super(Emotion.class, forVariable(variable));
    }

    public QEmotion(Path<? extends Emotion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmotion(PathMetadata metadata) {
        super(Emotion.class, metadata);
    }

}

