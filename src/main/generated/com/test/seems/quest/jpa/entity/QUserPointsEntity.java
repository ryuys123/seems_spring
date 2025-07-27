package com.test.seems.quest.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserPointsEntity is a Querydsl query type for UserPointsEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPointsEntity extends EntityPathBase<UserPointsEntity> {

    private static final long serialVersionUID = 155992889L;

    public static final QUserPointsEntity userPointsEntity = new QUserPointsEntity("userPointsEntity");

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final StringPath userId = createString("userId");

    public QUserPointsEntity(String variable) {
        super(UserPointsEntity.class, forVariable(variable));
    }

    public QUserPointsEntity(Path<? extends UserPointsEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserPointsEntity(PathMetadata metadata) {
        super(UserPointsEntity.class, metadata);
    }

}

