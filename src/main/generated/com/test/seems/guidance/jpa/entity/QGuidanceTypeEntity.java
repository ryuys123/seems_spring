package com.test.seems.guidance.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGuidanceTypeEntity is a Querydsl query type for GuidanceTypeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuidanceTypeEntity extends EntityPathBase<GuidanceTypeEntity> {

    private static final long serialVersionUID = -409593893L;

    public static final QGuidanceTypeEntity guidanceTypeEntity = new QGuidanceTypeEntity("guidanceTypeEntity");

    public final StringPath description = createString("description");

    public final NumberPath<Long> guidanceTypeId = createNumber("guidanceTypeId", Long.class);

    public final StringPath guidanceTypeName = createString("guidanceTypeName");

    public QGuidanceTypeEntity(String variable) {
        super(GuidanceTypeEntity.class, forVariable(variable));
    }

    public QGuidanceTypeEntity(Path<? extends GuidanceTypeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGuidanceTypeEntity(PathMetadata metadata) {
        super(GuidanceTypeEntity.class, metadata);
    }

}

