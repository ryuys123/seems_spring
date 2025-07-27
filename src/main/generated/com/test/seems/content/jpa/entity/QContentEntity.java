package com.test.seems.content.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QContentEntity is a Querydsl query type for ContentEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContentEntity extends EntityPathBase<ContentEntity> {

    private static final long serialVersionUID = -1371517731L;

    public static final QContentEntity contentEntity = new QContentEntity("contentEntity");

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final StringPath description = createString("description");

    public final StringPath duration = createString("duration");

    public final NumberPath<Integer> isActive = createNumber("isActive", Integer.class);

    public final DateTimePath<java.util.Date> publishedAt = createDateTime("publishedAt", java.util.Date.class);

    public final StringPath theme = createString("theme");

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    public final DateTimePath<java.util.Date> updatedAt = createDateTime("updatedAt", java.util.Date.class);

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public final StringPath youtubeId = createString("youtubeId");

    public QContentEntity(String variable) {
        super(ContentEntity.class, forVariable(variable));
    }

    public QContentEntity(Path<? extends ContentEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContentEntity(PathMetadata metadata) {
        super(ContentEntity.class, metadata);
    }

}

