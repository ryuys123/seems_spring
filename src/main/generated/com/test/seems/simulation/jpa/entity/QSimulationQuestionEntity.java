package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSimulationQuestionEntity is a Querydsl query type for SimulationQuestionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSimulationQuestionEntity extends EntityPathBase<SimulationQuestionEntity> {

    private static final long serialVersionUID = -388153947L;

    public static final QSimulationQuestionEntity simulationQuestionEntity = new QSimulationQuestionEntity("simulationQuestionEntity");

    public final StringPath choiceOptions = createString("choiceOptions");

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final NumberPath<Integer> questionNumber = createNumber("questionNumber", Integer.class);

    public final StringPath questionText = createString("questionText");

    public final NumberPath<Long> scenarioId = createNumber("scenarioId", Long.class);

    public final NumberPath<Long> settingId = createNumber("settingId", Long.class);

    public QSimulationQuestionEntity(String variable) {
        super(SimulationQuestionEntity.class, forVariable(variable));
    }

    public QSimulationQuestionEntity(Path<? extends SimulationQuestionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSimulationQuestionEntity(PathMetadata metadata) {
        super(SimulationQuestionEntity.class, metadata);
    }

}

