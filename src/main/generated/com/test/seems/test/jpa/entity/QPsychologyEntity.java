package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPsychologyEntity is a Querydsl query type for PsychologyEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPsychologyEntity extends EntityPathBase<PsychologyEntity> {

    private static final long serialVersionUID = 798280556L;

    public static final QPsychologyEntity psychologyEntity = new QPsychologyEntity("psychologyEntity");

    public final DateTimePath<java.time.LocalDateTime> answerDatetime = createDateTime("answerDatetime", java.time.LocalDateTime.class);

    public final NumberPath<Long> answerId = createNumber("answerId", Long.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath testType = createString("testType");

    public final StringPath userId = createString("userId");

    public final StringPath userResponseText = createString("userResponseText");

    public QPsychologyEntity(String variable) {
        super(PsychologyEntity.class, forVariable(variable));
    }

    public QPsychologyEntity(Path<? extends PsychologyEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPsychologyEntity(PathMetadata metadata) {
        super(PsychologyEntity.class, metadata);
    }

}

