package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuestEntity is a Querydsl query type for QuestEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestEntity extends EntityPathBase<QuestEntity> {

    private static final long serialVersionUID = 2134636861L;

    public static final QQuestEntity questEntity = new QQuestEntity("questEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> isCompleted = createNumber("isCompleted", Integer.class);

    public final NumberPath<Long> questId = createNumber("questId", Long.class);

    public final StringPath questName = createString("questName");

    public final NumberPath<Integer> questPoints = createNumber("questPoints", Integer.class);

    public final StringPath userId = createString("userId");

    public QQuestEntity(String variable) {
        super(QuestEntity.class, forVariable(variable));
    }

    public QQuestEntity(Path<? extends QuestEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestEntity(PathMetadata metadata) {
        super(QuestEntity.class, metadata);
    }

}

