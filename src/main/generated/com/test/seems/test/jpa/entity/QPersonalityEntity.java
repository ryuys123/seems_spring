package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPersonalityEntity is a Querydsl query type for PersonalityEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPersonalityEntity extends EntityPathBase<PersonalityEntity> {

    private static final long serialVersionUID = -356821103L;

    public static final QPersonalityEntity personalityEntity = new QPersonalityEntity("personalityEntity");

    public final NumberPath<Long> answerId = createNumber("answerId", Long.class);

    public final StringPath answerValue = createString("answerValue");

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath userId = createString("userId");

    public QPersonalityEntity(String variable) {
        super(PersonalityEntity.class, forVariable(variable));
    }

    public QPersonalityEntity(Path<? extends PersonalityEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPersonalityEntity(PathMetadata metadata) {
        super(PersonalityEntity.class, metadata);
    }

}

