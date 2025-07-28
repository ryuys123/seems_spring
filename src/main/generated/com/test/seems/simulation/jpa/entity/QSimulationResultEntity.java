package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSimulationResultEntity is a Querydsl query type for SimulationResultEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSimulationResultEntity extends EntityPathBase<SimulationResultEntity> {

    private static final long serialVersionUID = 332850076L;

    public static final QSimulationResultEntity simulationResultEntity = new QSimulationResultEntity("simulationResultEntity");

    public final StringPath personalityType = createString("personalityType");

    public final NumberPath<Long> resultId = createNumber("resultId", Long.class);

    public final StringPath resultSummary = createString("resultSummary");

    public final StringPath resultTitle = createString("resultTitle");

    public final StringPath resultType = createString("resultType");

    public QSimulationResultEntity(String variable) {
        super(SimulationResultEntity.class, forVariable(variable));
    }

    public QSimulationResultEntity(Path<? extends SimulationResultEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSimulationResultEntity(PathMetadata metadata) {
        super(SimulationResultEntity.class, metadata);
    }

}

