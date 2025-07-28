package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuestRewardEntity is a Querydsl query type for QuestRewardEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestRewardEntity extends EntityPathBase<QuestRewardEntity> {

    private static final long serialVersionUID = 696969676L;

    public static final QQuestRewardEntity questRewardEntity = new QQuestRewardEntity("questRewardEntity");

    public final StringPath description = createString("description");

    public final StringPath imagePath = createString("imagePath");

    public final StringPath questName = createString("questName");

    public final NumberPath<Integer> requiredPoints = createNumber("requiredPoints", Integer.class);

    public final NumberPath<Long> rewardId = createNumber("rewardId", Long.class);

    public final StringPath rewardRarity = createString("rewardRarity");

    public final StringPath titleReward = createString("titleReward");

    public QQuestRewardEntity(String variable) {
        super(QuestRewardEntity.class, forVariable(variable));
    }

    public QQuestRewardEntity(Path<? extends QuestRewardEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestRewardEntity(PathMetadata metadata) {
        super(QuestRewardEntity.class, metadata);
    }

}

