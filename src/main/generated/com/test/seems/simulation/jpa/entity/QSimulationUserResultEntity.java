package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSimulationUserResultEntity is a Querydsl query type for SimulationUserResultEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSimulationUserResultEntity extends EntityPathBase<SimulationUserResultEntity> {

    private static final long serialVersionUID = 1362218247L;

    public static final QSimulationUserResultEntity simulationUserResultEntity = new QSimulationUserResultEntity("simulationUserResultEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath personalityType = createString("personalityType");

    public final StringPath resultSummary = createString("resultSummary");

    public final StringPath resultTitle = createString("resultTitle");

    public final NumberPath<Long> settingId = createNumber("settingId", Long.class);

    public final NumberPath<Long> userResultId = createNumber("userResultId", Long.class);

    public QSimulationUserResultEntity(String variable) {
        super(SimulationUserResultEntity.class, forVariable(variable));
    }

    public QSimulationUserResultEntity(Path<? extends SimulationUserResultEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSimulationUserResultEntity(PathMetadata metadata) {
        super(SimulationUserResultEntity.class, metadata);
    }

}

