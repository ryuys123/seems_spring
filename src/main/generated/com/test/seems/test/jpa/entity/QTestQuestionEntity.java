package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTestQuestionEntity is a Querydsl query type for TestQuestionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTestQuestionEntity extends EntityPathBase<TestQuestionEntity> {

    private static final long serialVersionUID = 1023925499L;

    public static final QTestQuestionEntity testQuestionEntity = new QTestQuestionEntity("testQuestionEntity");

    public final StringPath category = createString("category");

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath questionText = createString("questionText");

    public final StringPath scoreDirection = createString("scoreDirection");

    public final StringPath testType = createString("testType");

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QTestQuestionEntity(String variable) {
        super(TestQuestionEntity.class, forVariable(variable));
    }

    public QTestQuestionEntity(Path<? extends TestQuestionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestQuestionEntity(PathMetadata metadata) {
        super(TestQuestionEntity.class, metadata);
    }

}

