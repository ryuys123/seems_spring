package com.test.seems.simulation.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScenarioEntity is a Querydsl query type for ScenarioEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScenarioEntity extends EntityPathBase<ScenarioEntity> {

    private static final long serialVersionUID = 1842588200L;

    public static final QScenarioEntity scenarioEntity = new QScenarioEntity("scenarioEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<java.time.DayOfWeek> dayOfWeek = createEnum("dayOfWeek", java.time.DayOfWeek.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> isActive = createNumber("isActive", Integer.class);

    public final NumberPath<Long> scenarioId = createNumber("scenarioId", Long.class);

    public final StringPath scenarioName = createString("scenarioName");

    public final EnumPath<ScenarioEntity.SimulationType> simulationType = createEnum("simulationType", ScenarioEntity.SimulationType.class);

    public QScenarioEntity(String variable) {
        super(ScenarioEntity.class, forVariable(variable));
    }

    public QScenarioEntity(Path<? extends ScenarioEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScenarioEntity(PathMetadata metadata) {
        super(ScenarioEntity.class, metadata);
    }

}

