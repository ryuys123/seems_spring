package com.test.seems.social.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSocialLoginEntity is a Querydsl query type for SocialLoginEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialLoginEntity extends EntityPathBase<SocialLoginEntity> {

    private static final long serialVersionUID = 1604566692L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSocialLoginEntity socialLoginEntity = new QSocialLoginEntity("socialLoginEntity");

    public final DateTimePath<java.util.Date> linkedAt = createDateTime("linkedAt", java.util.Date.class);

    public final StringPath provider = createString("provider");

    public final StringPath socialEmail = createString("socialEmail");

    public final StringPath socialId = createString("socialId");

    public final NumberPath<Long> socialLoginId = createNumber("socialLoginId", Long.class);

    public final com.test.seems.user.jpa.entity.QUserEntity user;

    public QSocialLoginEntity(String variable) {
        this(SocialLoginEntity.class, forVariable(variable), INITS);
    }

    public QSocialLoginEntity(Path<? extends SocialLoginEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSocialLoginEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSocialLoginEntity(PathMetadata metadata, PathInits inits) {
        this(SocialLoginEntity.class, metadata, inits);
    }

    public QSocialLoginEntity(Class<? extends SocialLoginEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.test.seems.user.jpa.entity.QUserEntity(forProperty("user")) : null;
    }

}

