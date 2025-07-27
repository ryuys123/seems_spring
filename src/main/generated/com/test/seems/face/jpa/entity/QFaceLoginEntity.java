package com.test.seems.face.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFaceLoginEntity is a Querydsl query type for FaceLoginEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFaceLoginEntity extends EntityPathBase<FaceLoginEntity> {

    private static final long serialVersionUID = -1585788668L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFaceLoginEntity faceLoginEntity = new QFaceLoginEntity("faceLoginEntity");

    public final StringPath createdBy = createString("createdBy");

    public final StringPath faceIdHash = createString("faceIdHash");

    public final StringPath faceImagePath = createString("faceImagePath");

    public final NumberPath<Long> faceLoginId = createNumber("faceLoginId", Long.class);

    public final StringPath faceName = createString("faceName");

    public final NumberPath<Integer> isActive = createNumber("isActive", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> lastUsedAt = createDateTime("lastUsedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public final StringPath userId = createString("userId");

    public QFaceLoginEntity(String variable) {
        this(FaceLoginEntity.class, forVariable(variable), INITS);
    }

    public QFaceLoginEntity(Path<? extends FaceLoginEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFaceLoginEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFaceLoginEntity(PathMetadata metadata, PathInits inits) {
        this(FaceLoginEntity.class, metadata, inits);
    }

    public QFaceLoginEntity(Class<? extends FaceLoginEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

