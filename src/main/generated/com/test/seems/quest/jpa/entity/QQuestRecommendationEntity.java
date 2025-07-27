package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuestRecommendationEntity is a Querydsl query type for QuestRecommendationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestRecommendationEntity extends EntityPathBase<QuestRecommendationEntity> {

    private static final long serialVersionUID = 1419858134L;

    public static final QQuestRecommendationEntity questRecommendationEntity = new QQuestRecommendationEntity("questRecommendationEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final NumberPath<Long> recommendId = createNumber("recommendId", Long.class);

    public final NumberPath<Integer> reward = createNumber("reward", Integer.class);

    public final StringPath title = createString("title");

    public QQuestRecommendationEntity(String variable) {
        super(QuestRecommendationEntity.class, forVariable(variable));
    }

    public QQuestRecommendationEntity(Path<? extends QuestRecommendationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestRecommendationEntity(PathMetadata metadata) {
        super(QuestRecommendationEntity.class, metadata);
    }

}

