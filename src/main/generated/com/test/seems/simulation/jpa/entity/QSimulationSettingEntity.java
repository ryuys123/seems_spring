package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSimulationSettingEntity is a Querydsl query type for SimulationSettingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSimulationSettingEntity extends EntityPathBase<SimulationSettingEntity> {

    private static final long serialVersionUID = -1973619561L;

    public static final QSimulationSettingEntity simulationSettingEntity = new QSimulationSettingEntity("simulationSettingEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> scenarioId = createNumber("scenarioId", Long.class);

    public final NumberPath<Long> settingId = createNumber("settingId", Long.class);

    public final StringPath status = createString("status");

    public final StringPath userId = createString("userId");

    public QSimulationSettingEntity(String variable) {
        super(SimulationSettingEntity.class, forVariable(variable));
    }

    public QSimulationSettingEntity(Path<? extends SimulationSettingEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSimulationSettingEntity(PathMetadata metadata) {
        super(SimulationSettingEntity.class, metadata);
    }

}

