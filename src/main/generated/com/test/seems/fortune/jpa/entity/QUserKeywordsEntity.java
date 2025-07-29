package com.test.seems.fortune.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserKeywordsEntity is a Querydsl query type for UserKeywordsEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserKeywordsEntity extends EntityPathBase<UserKeywordsEntity> {

    private static final long serialVersionUID = 1970324511L;

    public static final QUserKeywordsEntity userKeywordsEntity = new QUserKeywordsEntity("userKeywordsEntity");

    public final NumberPath<Long> guidanceTypeId = createNumber("guidanceTypeId", Long.class);

    public final BooleanPath isSelected = createBoolean("isSelected");

    public final NumberPath<Long> keywordId = createNumber("keywordId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedDate = createDateTime("updatedDate", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QUserKeywordsEntity(String variable) {
        super(UserKeywordsEntity.class, forVariable(variable));
    }

    public QUserKeywordsEntity(Path<? extends UserKeywordsEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserKeywordsEntity(PathMetadata metadata) {
        super(UserKeywordsEntity.class, metadata);
    }

}

