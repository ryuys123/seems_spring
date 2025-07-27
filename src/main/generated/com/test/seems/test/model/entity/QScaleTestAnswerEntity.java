package com.test.seems.test.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScaleTestAnswerEntity is a Querydsl query type for ScaleTestAnswerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScaleTestAnswerEntity extends EntityPathBase<ScaleTestAnswerEntity> {

    private static final long serialVersionUID = -384787089L;

    public static final QScaleTestAnswerEntity scaleTestAnswerEntity = new QScaleTestAnswerEntity("scaleTestAnswerEntity");

    public final DateTimePath<java.time.LocalDateTime> answerDatetime = createDateTime("answerDatetime", java.time.LocalDateTime.class);

    public final NumberPath<Long> answerId = createNumber("answerId", Long.class);

    public final NumberPath<Integer> answerValue = createNumber("answerValue", Integer.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath testCategory = createString("testCategory");

    public final StringPath testType = createString("testType");

    public final StringPath userId = createString("userId");

    public QScaleTestAnswerEntity(String variable) {
        super(ScaleTestAnswerEntity.class, forVariable(variable));
    }

    public QScaleTestAnswerEntity(Path<? extends ScaleTestAnswerEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScaleTestAnswerEntity(PathMetadata metadata) {
        super(ScaleTestAnswerEntity.class, metadata);
    }

}

