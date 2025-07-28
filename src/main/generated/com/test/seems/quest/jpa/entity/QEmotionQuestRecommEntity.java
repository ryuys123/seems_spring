package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmotionQuestRecommEntity is a Querydsl query type for EmotionQuestRecommEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionQuestRecommEntity extends EntityPathBase<EmotionQuestRecommEntity> {

    private static final long serialVersionUID = 500750481L;

    public static final QEmotionQuestRecommEntity emotionQuestRecommEntity = new QEmotionQuestRecommEntity("emotionQuestRecommEntity");

    public final NumberPath<Long> emotionId = createNumber("emotionId", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Long> recommendId = createNumber("recommendId", Long.class);

    public QEmotionQuestRecommEntity(String variable) {
        super(EmotionQuestRecommEntity.class, forVariable(variable));
    }

    public QEmotionQuestRecommEntity(Path<? extends EmotionQuestRecommEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmotionQuestRecommEntity(PathMetadata metadata) {
        super(EmotionQuestRecommEntity.class, metadata);
    }

}

