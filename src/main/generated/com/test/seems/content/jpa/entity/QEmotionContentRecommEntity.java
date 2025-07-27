package com.test.seems.content.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmotionContentRecommEntity is a Querydsl query type for EmotionContentRecommEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionContentRecommEntity extends EntityPathBase<EmotionContentRecommEntity> {

    private static final long serialVersionUID = 1139523007L;

    public static final QEmotionContentRecommEntity emotionContentRecommEntity = new QEmotionContentRecommEntity("emotionContentRecommEntity");

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    public final NumberPath<Long> emotionId = createNumber("emotionId", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public QEmotionContentRecommEntity(String variable) {
        super(EmotionContentRecommEntity.class, forVariable(variable));
    }

    public QEmotionContentRecommEntity(Path<? extends EmotionContentRecommEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmotionContentRecommEntity(PathMetadata metadata) {
        super(EmotionContentRecommEntity.class, metadata);
    }

}

