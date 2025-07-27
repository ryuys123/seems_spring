package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSimulationChoiceEntity is a Querydsl query type for SimulationChoiceEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSimulationChoiceEntity extends EntityPathBase<SimulationChoiceEntity> {

    private static final long serialVersionUID = -631683552L;

    public static final QSimulationChoiceEntity simulationChoiceEntity = new QSimulationChoiceEntity("simulationChoiceEntity");

    public final NumberPath<Long> choiceId = createNumber("choiceId", Long.class);

    public final StringPath choiceText = createString("choiceText");

    public final NumberPath<Integer> questionNumber = createNumber("questionNumber", Integer.class);

    public final StringPath selectedTrait = createString("selectedTrait");

    public final NumberPath<Long> settingId = createNumber("settingId", Long.class);

    public QSimulationChoiceEntity(String variable) {
        super(SimulationChoiceEntity.class, forVariable(variable));
    }

    public QSimulationChoiceEntity(Path<? extends SimulationChoiceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSimulationChoiceEntity(PathMetadata metadata) {
        super(SimulationChoiceEntity.class, metadata);
    }

}

