package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserRewardEntity is a Querydsl query type for UserRewardEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRewardEntity extends EntityPathBase<UserRewardEntity> {

    private static final long serialVersionUID = 856378501L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserRewardEntity userRewardEntity = new QUserRewardEntity("userRewardEntity");

    public final DateTimePath<java.time.LocalDateTime> acquiredAt = createDateTime("acquiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> isApplied = createNumber("isApplied", Integer.class);

    public final NumberPath<Integer> isEquipped = createNumber("isEquipped", Integer.class);

    public final QQuestRewardEntity questReward;

    public final NumberPath<Long> rewardId = createNumber("rewardId", Long.class);

    public final StringPath userId = createString("userId");

    public final NumberPath<Long> userRewardId = createNumber("userRewardId", Long.class);

    public QUserRewardEntity(String variable) {
        this(UserRewardEntity.class, forVariable(variable), INITS);
    }

    public QUserRewardEntity(Path<? extends UserRewardEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserRewardEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserRewardEntity(PathMetadata metadata, PathInits inits) {
        this(UserRewardEntity.class, metadata, inits);
    }

    public QUserRewardEntity(Class<? extends UserRewardEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.questReward = inits.isInitialized("questReward") ? new QQuestRewardEntity(forProperty("questReward")) : null;
    }

}

