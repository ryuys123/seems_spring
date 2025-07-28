package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPersonalityTestResultEntity is a Querydsl query type for PersonalityTestResultEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPersonalityTestResultEntity extends EntityPathBase<PersonalityTestResultEntity> {

    private static final long serialVersionUID = -1744843200L;

    public static final QPersonalityTestResultEntity personalityTestResultEntity = new QPersonalityTestResultEntity("personalityTestResultEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath mbtiTitle = createString("mbtiTitle");

    public final NumberPath<Long> personalityId = createNumber("personalityId", Long.class);

    public final NumberPath<Long> personalityTestId = createNumber("personalityTestId", Long.class);

    public final StringPath result = createString("result");

    public final StringPath userId = createString("userId");

    public QPersonalityTestResultEntity(String variable) {
        super(PersonalityTestResultEntity.class, forVariable(variable));
    }

    public QPersonalityTestResultEntity(Path<? extends PersonalityTestResultEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPersonalityTestResultEntity(PathMetadata metadata) {
        super(PersonalityTestResultEntity.class, metadata);
    }

}

