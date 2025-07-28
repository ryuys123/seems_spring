package com.test.seems.faq.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFaqEntity is a Querydsl query type for FaqEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFaqEntity extends EntityPathBase<FaqEntity> {

    private static final long serialVersionUID = -846548547L;

    public static final QFaqEntity faqEntity = new QFaqEntity("faqEntity");

    public final StringPath category = createString("category");

    public final StringPath content = createString("content");

    public final DatePath<java.sql.Date> faqDate = createDate("faqDate", java.sql.Date.class);

    public final NumberPath<Integer> faqNo = createNumber("faqNo", Integer.class);

    public final DatePath<java.sql.Date> reFaqDate = createDate("reFaqDate", java.sql.Date.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public final StringPath userid = createString("userid");

    public QFaqEntity(String variable) {
        super(FaqEntity.class, forVariable(variable));
    }

    public QFaqEntity(Path<? extends FaqEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFaqEntity(PathMetadata metadata) {
        super(FaqEntity.class, metadata);
    }

}

