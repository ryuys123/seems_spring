package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmotionEntity is a Querydsl query type for EmotionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionEntity extends EntityPathBase<EmotionEntity> {

    private static final long serialVersionUID = 562993086L;

    public static final QEmotionEntity emotionEntity = new QEmotionEntity("emotionEntity");

    public final StringPath description = createString("description");

    public final NumberPath<Long> emotionId = createNumber("emotionId", Long.class);

    public final StringPath emotionName = createString("emotionName");

    public QEmotionEntity(String variable) {
        super(EmotionEntity.class, forVariable(variable));
    }

    public QEmotionEntity(Path<? extends EmotionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmotionEntity(PathMetadata metadata) {
        super(EmotionEntity.class, metadata);
    }

}

