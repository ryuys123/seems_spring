package com.test.seems.test.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPsychologicalScaleAnswer is a Querydsl query type for PsychologicalScaleAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPsychologicalScaleAnswer extends EntityPathBase<PsychologicalScaleAnswer> {

    private static final long serialVersionUID = -392409261L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPsychologicalScaleAnswer psychologicalScaleAnswer = new QPsychologicalScaleAnswer("psychologicalScaleAnswer");

    public final DateTimePath<java.sql.Timestamp> answerDatetime = createDateTime("answerDatetime", java.sql.Timestamp.class);

    public final NumberPath<Long> answerId = createNumber("answerId", Long.class);

    public final NumberPath<Integer> answerValue = createNumber("answerValue", Integer.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath testCategory = createString("testCategory");

    public final StringPath testType = createString("testType");

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public QPsychologicalScaleAnswer(String variable) {
        this(PsychologicalScaleAnswer.class, forVariable(variable), INITS);
    }

    public QPsychologicalScaleAnswer(Path<? extends PsychologicalScaleAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPsychologicalScaleAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPsychologicalScaleAnswer(PathMetadata metadata, PathInits inits) {
        this(PsychologicalScaleAnswer.class, metadata, inits);
    }

    public QPsychologicalScaleAnswer(Class<? extends PsychologicalScaleAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

